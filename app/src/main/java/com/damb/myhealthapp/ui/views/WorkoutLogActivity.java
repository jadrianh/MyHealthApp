package com.damb.myhealthapp.ui.views;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.view.View;
import androidx.cardview.widget.CardView;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import com.damb.myhealthapp.ui.adapters.RutinaLogAdapter;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;

public class WorkoutLogActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewRutinas;
    private RutinaLogAdapter rutinaLogAdapter;
    private ArrayList<RutinaLogAdapter.RutinaLogItem> listaRutinas = new ArrayList<>();
    private TextView textCalories;
    private CardView cardCalories;
    private int metaCalorias;
    private int totalCaloriasQuemadas;
    private final int[] metasPredeterminadas = {200, 300, 400, 500, 600, 700, 800};
    private SharedPreferences prefs;
    private TextView textStreak;
    private CardView cardStreak;
    private int rachaActual;
    private int recordRacha;
    private Set<CalendarDay> diasConEjercicio = new HashSet<>();
    
    // Nuevos elementos para la meta de calorías
    private TextView textCurrentCalories;
    private TextView textGoalCalories;
    private TextView textProgressPercentage;
    private ProgressBar progressBarCalories;
    private ImageButton btnEditCalorieGoal;
    private boolean metaAlcanzadaMostrada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_log);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewRutinas = findViewById(R.id.recyclerViewRutinas);
        if (recyclerViewRutinas != null) {
            recyclerViewRutinas.setLayoutManager(new LinearLayoutManager(this));
            rutinaLogAdapter = new RutinaLogAdapter(listaRutinas, (rutinaId, nombreRutina) -> {
                try {
                    Intent intent = new Intent(WorkoutLogActivity.this, WorkoutDetailActivity.class);
                    intent.putExtra("rutina_id", rutinaId);
                    intent.putExtra("nombre_rutina", nombreRutina);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("WorkoutLogActivity", "Error al abrir detalle de rutina", e);
                    Toast.makeText(this, "Error al abrir detalle", Toast.LENGTH_SHORT).show();
                }
            });
            recyclerViewRutinas.setAdapter(rutinaLogAdapter);
        }

        // Inicializar elementos de calorías
        textCalories = findViewById(R.id.textCalories);
        textCurrentCalories = findViewById(R.id.textCurrentCalories);
        textGoalCalories = findViewById(R.id.textGoalCalories);
        textProgressPercentage = findViewById(R.id.textProgressPercentage);
        progressBarCalories = findViewById(R.id.progressBarCalories);
        btnEditCalorieGoal = findViewById(R.id.btnEditCalorieGoal);
        
        if (textCalories != null) {
            cardCalories = (CardView) findViewById(R.id.textCalories).getParent().getParent();
            prefs = getSharedPreferences("MyHealthAppPrefs", MODE_PRIVATE);
            metaCalorias = prefs.getInt("meta_calorias", 500); // Valor por defecto: 500 kcal
            actualizarCaloriasUI();
            // Remover el click listener del card original ya que ahora usamos el botón específico
            if (cardCalories != null) {
                cardCalories.setOnClickListener(null);
            }
        }
        
        // Configurar botón de editar meta
        if (btnEditCalorieGoal != null) {
            btnEditCalorieGoal.setOnClickListener(v -> mostrarDialogoMetas());
        }

        textStreak = findViewById(R.id.textStreak);
        if (textStreak != null) {
            cardStreak = (CardView) findViewById(R.id.textStreak).getParent().getParent();
            actualizarRachaUI();
            if (cardStreak != null) {
                cardStreak.setOnClickListener(v -> mostrarDialogoRacha());
            }
        }

        // Configurar botón de back para refrescar datos
        View backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Refrescar datos antes de volver
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d("WorkoutLogActivity", "Refrescando datos desde botón back");
                    cargarRegistrosEjercicio(user.getUid());
                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                }
                finish();
            });
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !isFinishing()) {
            cargarRegistrosEjercicio(user.getUid());
        }
        
        // Resetear estado de meta al inicio del día
        resetearEstadoMeta();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar datos cuando regreses a la pantalla
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !isFinishing()) {
            Log.d("WorkoutLogActivity", "Refrescando datos en onResume");
            cargarRegistrosEjercicio(user.getUid());
        }
    }

    private void cargarRegistrosEjercicio(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("WorkoutLogActivity", "UserId es null o vacío");
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("WorkoutLogActivity", "Cargando datos para fecha: " + todayDate);
        
        // Primero cargar los ejercicios del día actual
        db.collection("users").document(userId)
                .collection("registrosEjercicio").document(todayDate)
                .collection("workouts").get()
                .addOnSuccessListener(queryWorkouts -> {
                    if (isFinishing()) return;
                    
                    try {
                        listaRutinas.clear();
                        totalCaloriasQuemadas = 0;
                        
                        Log.d("WorkoutLogActivity", "Ejercicios encontrados hoy: " + (queryWorkouts != null ? queryWorkouts.size() : 0));
                        
                        if (queryWorkouts != null) {
                            for (QueryDocumentSnapshot document : queryWorkouts) {
                                try {
                                    String tipoRutina = document.getString("tipoRutina");
                                    String rutinaId = document.getId();
                                    if (tipoRutina != null && rutinaId != null) {
                                        listaRutinas.add(new RutinaLogAdapter.RutinaLogItem(rutinaId, tipoRutina));
                                        Log.d("WorkoutLogActivity", "Agregado ejercicio: " + tipoRutina);
                                    }
                                    Long calorias = document.getLong("calorias");
                                    if (calorias != null) {
                                        totalCaloriasQuemadas += calorias.intValue();
                                        Log.d("WorkoutLogActivity", "Calorías agregadas: " + calorias.intValue());
                                    }
                                } catch (Exception e) {
                                    Log.e("WorkoutLogActivity", "Error procesando documento de ejercicio", e);
                                }
                            }
                        }
                        
                        Log.d("WorkoutLogActivity", "Total calorías hoy: " + totalCaloriasQuemadas);
                        Log.d("WorkoutLogActivity", "Total ejercicios hoy: " + listaRutinas.size());
                        
                        if (rutinaLogAdapter != null) {
                            rutinaLogAdapter.notifyDataSetChanged();
                        }
                        actualizarCaloriasUI();
                    } catch (Exception e) {
                        Log.e("WorkoutLogActivity", "Error en onSuccess de cargar ejercicios", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkoutLogActivity", "Error al cargar ejercicios del día", e);
                    if (!isFinishing()) {
                        runOnUiThread(() -> Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show());
                    }
                });

        // Luego cargar todos los días para calcular racha
        Log.d("WorkoutLogActivity", "Iniciando consulta optimizada de días con ejercicio...");
        db.collection("users").document(userId)
                .collection("registrosEjercicio").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing()) return;
                    try {
                        diasConEjercicio.clear();
                        Set<String> diasEjercicio = new HashSet<>();
                        Log.d("WorkoutLogActivity", "Consulta de días completada");
                        Log.d("WorkoutLogActivity", "Días encontrados: " + (queryDocumentSnapshots != null ? queryDocumentSnapshots.size() : 0));
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot docDia : queryDocumentSnapshots) {
                                try {
                                    String fecha = docDia.getId();
                                    Boolean hasWorkout = docDia.getBoolean("hasWorkout");
                                    Log.d("WorkoutLogActivity", "Día: " + fecha + ", hasWorkout: " + hasWorkout);
                                    if (fecha != null && hasWorkout != null && hasWorkout) {
                                        diasEjercicio.add(fecha);
                                        // Agregar día con ejercicio al set para el calendario
                                        try {
                                            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha);
                                            if (date != null) {
                                                Calendar cal = Calendar.getInstance();
                                                cal.setTime(date);
                                                diasConEjercicio.add(CalendarDay.from(cal));
                                            }
                                        } catch (Exception e) {
                                            Log.e("WorkoutLogActivity", "Error parseando fecha: " + fecha, e);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("WorkoutLogActivity", "Error procesando día: " + docDia.getId(), e);
                                }
                            }
                            Log.d("WorkoutLogActivity", "Días con ejercicios: " + diasEjercicio.toString());
                            calcularRacha(diasEjercicio);
                            actualizarRachaUI();
                        } else {
                            Log.w("WorkoutLogActivity", "No se encontraron días con ejercicios");
                            // Si no hay días históricos, al menos agregar el día de hoy si hay ejercicios
                            if (totalCaloriasQuemadas > 0) {
                                diasEjercicio.add(todayDate);
                                Log.d("WorkoutLogActivity", "Agregando día de hoy a la racha: " + todayDate);
                                try {
                                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayDate);
                                    if (date != null) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        diasConEjercicio.add(CalendarDay.from(cal));
                                    }
                                } catch (Exception e) {
                                    Log.e("WorkoutLogActivity", "Error agregando día de hoy", e);
                                }
                                Log.d("WorkoutLogActivity", "Calculando racha con solo día de hoy");
                                calcularRacha(diasEjercicio);
                                actualizarRachaUI();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("WorkoutLogActivity", "Error en onSuccess de cargar registros", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkoutLogActivity", "Error al cargar registros de ejercicio", e);
                    // Si falla la consulta de días históricos, al menos usar el día de hoy
                    if (totalCaloriasQuemadas > 0) {
                        Set<String> diasEjercicio = new HashSet<>();
                        diasEjercicio.add(todayDate);
                        Log.d("WorkoutLogActivity", "Usando solo día de hoy para racha debido a error en consulta");
                        calcularRacha(diasEjercicio);
                        actualizarRachaUI();
                    }
                    if (!isFinishing()) {
                        runOnUiThread(() -> Toast.makeText(this, "Error al cargar registros", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void calcularRacha(Set<String> diasEjercicio) {
        try {
            rachaActual = 0;
            recordRacha = 0;
            if (diasEjercicio == null || diasEjercicio.isEmpty()) {
                Log.d("WorkoutLogActivity", "No hay días con ejercicio para calcular racha");
                return;
            }
            
            Log.d("WorkoutLogActivity", "Iniciando cálculo de racha con días: " + diasEjercicio.toString());
            
            ArrayList<String> fechas = new ArrayList<>(diasEjercicio);
            fechas.sort((a, b) -> b.compareTo(a)); // Orden descendente (más reciente primero)
            
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Log.d("WorkoutLogActivity", "Fecha de hoy: " + hoy);
            
            // Calcular racha actual
            int rachaTemp = 0;
            Calendar calReferencia = Calendar.getInstance();
            calReferencia.set(Calendar.HOUR_OF_DAY, 0);
            calReferencia.set(Calendar.MINUTE, 0);
            calReferencia.set(Calendar.SECOND, 0);
            calReferencia.set(Calendar.MILLISECOND, 0);
            
            // Empezar desde el día más reciente
            for (String fecha : fechas) {
                try {
                    if (fecha == null) continue;
                    
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha);
                    if (date == null) continue;
                    
                    Calendar calFecha = Calendar.getInstance();
                    calFecha.setTime(date);
                    calFecha.set(Calendar.HOUR_OF_DAY, 0);
                    calFecha.set(Calendar.MINUTE, 0);
                    calFecha.set(Calendar.SECOND, 0);
                    calFecha.set(Calendar.MILLISECOND, 0);
                    
                    if (rachaTemp == 0) {
                        // Primer día de la racha
                        rachaTemp = 1;
                        calReferencia = (Calendar) calFecha.clone();
                        Log.d("WorkoutLogActivity", "Iniciando racha con día: " + fecha);
                    } else {
                        // Verificar si es el día anterior consecutivo
                        Calendar diaAnterior = (Calendar) calReferencia.clone();
                        diaAnterior.add(Calendar.DATE, -1);
                        
                        if (calFecha.get(Calendar.YEAR) == diaAnterior.get(Calendar.YEAR) &&
                            calFecha.get(Calendar.DAY_OF_YEAR) == diaAnterior.get(Calendar.DAY_OF_YEAR)) {
                            rachaTemp++;
                            calReferencia = (Calendar) calFecha.clone();
                            Log.d("WorkoutLogActivity", "Racha continúa con día: " + fecha + " (racha actual: " + rachaTemp + ")");
                        } else {
                            Log.d("WorkoutLogActivity", "Racha se rompe en día: " + fecha + " (racha final: " + rachaTemp + ")");
                            break; // Se rompió la racha
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e("WorkoutLogActivity", "Error calculando racha para fecha: " + fecha, e);
                }
            }
            
            rachaActual = rachaTemp;
            Log.d("WorkoutLogActivity", "Racha actual calculada: " + rachaActual);
            
            // Calcular récord (máxima racha histórica)
            recordRacha = 0;
            int rachaMaxima = 0;
            Calendar calAnterior = null;
            
            Log.d("WorkoutLogActivity", "Calculando récord histórico...");
            for (String fecha : fechas) {
                try {
                    if (fecha == null) continue;
                    
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha);
                    if (date == null) continue;
                    
                    Calendar calFecha = Calendar.getInstance();
                    calFecha.setTime(date);
                    calFecha.set(Calendar.HOUR_OF_DAY, 0);
                    calFecha.set(Calendar.MINUTE, 0);
                    calFecha.set(Calendar.SECOND, 0);
                    calFecha.set(Calendar.MILLISECOND, 0);
                    
                    if (calAnterior == null) {
                        rachaMaxima = 1;
                        Log.d("WorkoutLogActivity", "Iniciando nueva racha histórica con: " + fecha);
                    } else {
                        // Verificar si es consecutivo
                        Calendar diaEsperado = (Calendar) calAnterior.clone();
                        diaEsperado.add(Calendar.DATE, -1);
                        
                        if (calFecha.get(Calendar.YEAR) == diaEsperado.get(Calendar.YEAR) &&
                            calFecha.get(Calendar.DAY_OF_YEAR) == diaEsperado.get(Calendar.DAY_OF_YEAR)) {
                            rachaMaxima++;
                            Log.d("WorkoutLogActivity", "Racha histórica continúa: " + fecha + " (racha: " + rachaMaxima + ")");
                        } else {
                            // Nueva racha
                            if (rachaMaxima > recordRacha) {
                                recordRacha = rachaMaxima;
                                Log.d("WorkoutLogActivity", "Nuevo récord establecido: " + recordRacha + " días");
                            }
                            rachaMaxima = 1;
                            Log.d("WorkoutLogActivity", "Nueva racha histórica iniciada: " + fecha);
                        }
                    }
                    
                    calAnterior = (Calendar) calFecha.clone();
                    
                } catch (Exception e) {
                    Log.e("WorkoutLogActivity", "Error calculando récord para fecha: " + fecha, e);
                }
            }
            
            // Verificar la última racha
            if (rachaMaxima > recordRacha) {
                recordRacha = rachaMaxima;
                Log.d("WorkoutLogActivity", "Récord final establecido: " + recordRacha + " días");
            }
            
            // Asegurar que la racha actual no sea mayor que el récord
            if (rachaActual > recordRacha) {
                recordRacha = rachaActual;
                Log.d("WorkoutLogActivity", "Racha actual es nuevo récord: " + recordRacha + " días");
            }
            
            Log.d("WorkoutLogActivity", "RESULTADO FINAL - Racha actual: " + rachaActual + ", Récord: " + recordRacha);
            
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error general en calcularRacha", e);
        }
    }

    private void actualizarCaloriasUI() {
        if (isFinishing()) return;
        
        try {
            runOnUiThread(() -> {
                try {
                    // Actualizar el CardView original (solo total de calorías)
                    if (textCalories != null) {
                        textCalories.setText(totalCaloriasQuemadas + " kcal");
                    }
                    
                    // Actualizar la nueva sección de meta
                    if (textCurrentCalories != null) {
                        textCurrentCalories.setText(String.valueOf(totalCaloriasQuemadas));
                    }
                    
                    if (textGoalCalories != null) {
                        textGoalCalories.setText(String.valueOf(metaCalorias));
                    }
                    
                    // Calcular y actualizar progreso con animación
                    int porcentaje = 0;
                    if (metaCalorias > 0) {
                        porcentaje = Math.min((totalCaloriasQuemadas * 100) / metaCalorias, 100);
                    }
                    
                    // Verificar si se alcanzó la meta
                    if (porcentaje >= 100 && !metaAlcanzadaMostrada) {
                        metaAlcanzadaMostrada = true;
                        Toast.makeText(this, getString(R.string.felicitaciones_meta), Toast.LENGTH_LONG).show();
                    } else if (porcentaje >= 90 && porcentaje < 100 && !metaAlcanzadaMostrada) {
                        Toast.makeText(this, getString(R.string.casi_meta), Toast.LENGTH_SHORT).show();
                    }
                    
                    if (progressBarCalories != null) {
                        // Animar la barra de progreso
                        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBarCalories, "progress", porcentaje);
                        progressAnimator.setDuration(800);
                        progressAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                        progressAnimator.start();
                    }
                    
                    if (textProgressPercentage != null) {
                        // Animar el porcentaje de texto
                        ValueAnimator textAnimator = ValueAnimator.ofInt(0, porcentaje);
                        textAnimator.setDuration(800);
                        textAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                        textAnimator.addUpdateListener(animation -> {
                            int animatedValue = (int) animation.getAnimatedValue();
                            textProgressPercentage.setText(animatedValue + "%");
                        });
                        textAnimator.start();
                    }
                    
                } catch (Exception e) {
                    Log.e("WorkoutLogActivity", "Error actualizando UI de calorías", e);
                }
            });
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error en actualizarCaloriasUI", e);
        }
    }

    private void actualizarRachaUI() {
        if (isFinishing() || textStreak == null) return;
        
        try {
            runOnUiThread(() -> {
                try {
                    if (rachaActual > 0) {
                        textStreak.setTextColor(ContextCompat.getColor(this, R.color.green));
                        textStreak.setText(rachaActual + "d");
                    } else {
                        textStreak.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                        textStreak.setText("0d");
                    }
                } catch (Exception e) {
                    Log.e("WorkoutLogActivity", "Error actualizando UI de racha", e);
                }
            });
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error en actualizarRachaUI", e);
        }
    }

    private void mostrarDialogoMetas() {
        try {
            if (isFinishing()) return;
            
            String[] opciones = new String[metasPredeterminadas.length];
            int seleccionActual = 0;
            for (int i = 0; i < metasPredeterminadas.length; i++) {
                opciones[i] = metasPredeterminadas[i] + " kcal";
                if (metasPredeterminadas[i] == metaCalorias) seleccionActual = i;
            }
            
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.establecer_meta))
                    .setSingleChoiceItems(opciones, seleccionActual, (dialog, which) -> {
                        try {
                            metaCalorias = metasPredeterminadas[which];
                            if (prefs != null) {
                                prefs.edit().putInt("meta_calorias", metaCalorias).apply();
                            }
                            actualizarCaloriasUI();
                            dialog.dismiss();
                            Toast.makeText(this, getString(R.string.meta_actualizada, metaCalorias), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("WorkoutLogActivity", "Error al seleccionar meta", e);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancelar), null)
                    .show();
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error al mostrar diálogo de metas", e);
            Toast.makeText(this, "Error al mostrar opciones", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoRacha() {
        try {
            if (isFinishing()) return;
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Racha de entrenamiento");
            
            // Usar CalendarView nativo temporalmente para evitar crashes
            CalendarView calendarView = new CalendarView(this);
            calendarView.setDate(System.currentTimeMillis());
            
            builder.setView(calendarView);
            builder.setMessage("Racha actual: " + rachaActual + " días\nRécord: " + recordRacha + " días");
            builder.setNegativeButton("Cerrar", null);
            builder.show();
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error al mostrar diálogo de racha", e);
            Toast.makeText(this, "Racha actual: " + rachaActual + " días", Toast.LENGTH_LONG).show();
        }
    }

    // Decorador para mostrar el icono de fuego en los días con ejercicio
    private static class DiaEjercicioDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dias;
        DiaEjercicioDecorator(Set<CalendarDay> dias) {
            this.dias = new HashSet<>(dias);
        }
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dias.contains(day);
        }
        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, 0xFFFF5722)); // Punto naranja para días con ejercicio
        }
    }

    private void resetearEstadoMeta() {
        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String lastDate = prefs.getString("last_meta_check_date", "");
            
            if (!todayDate.equals(lastDate)) {
                metaAlcanzadaMostrada = false;
                prefs.edit().putString("last_meta_check_date", todayDate).apply();
            }
        } catch (Exception e) {
            Log.e("WorkoutLogActivity", "Error reseteando estado de meta", e);
        }
    }
} 