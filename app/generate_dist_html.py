import json
import os

students_path = "app/src/main/assets/sac_students.json"
coaches_path = "app/src/main/assets/sac_coaches.json"

with open(students_path, "r", encoding="utf-8") as f:
    students_data = json.load(f)

with open(coaches_path, "r", encoding="utf-8") as f:
    coaches_data = json.load(f)

html_content = f"""<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>SAC Garut</title>
  <link rel="manifest" href="manifest.json" />
  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
  <meta name="apple-mobile-web-app-title" content="SAC Garut" />
  <link rel="apple-touch-icon" href="icon-192.png" />
  <meta name="theme-color" content="#0EA5E9" />
  
  <!-- Tailwind CSS & Alpine.js CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- Firebase Web SDK CDN -->
  <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
  <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore-compat.js"></script>
  
  <style>
    body {{ font-family: 'Plus Jakarta Sans', sans-serif; background-color: #F8FAFC; color: #0F172A; }}
  </style>
</head>
<body x-data="sacApp()" x-init="init()" class="min-h-screen flex flex-col pb-20">

  <!-- Top Header -->
  <header class="bg-slate-900 text-white sticky top-0 z-50 shadow-md">
    <div class="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-xl bg-cyan-500 flex items-center justify-center font-extrabold text-slate-900 text-lg shadow-inner">
          SG
        </div>
        <div>
          <h1 class="font-bold text-base leading-tight">SAC Garut</h1>
          <p class="text-xs text-cyan-300">Master Data & Evaluasi Siswa</p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <span class="bg-emerald-500/20 text-emerald-300 text-[10px] font-bold px-2 py-0.5 rounded-full border border-emerald-500/30">Firebase Sync</span>
        <span class="bg-cyan-500/20 text-cyan-300 text-xs font-semibold px-2.5 py-1 rounded-full border border-cyan-500/30" x-text="students.length + ' Siswa'"></span>
      </div>
    </div>
  </header>

  <!-- Main Content Area -->
  <main class="max-w-4xl mx-auto px-4 py-4 w-full flex-1">
    
    <!-- TAB 1: DASHBOARD / HOME -->
    <div x-show="currentTab === 'home'" class="space-y-4">
      <!-- Hero Banner -->
      <div class="bg-gradient-to-r from-slate-900 to-sky-900 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
        <div class="relative z-10">
          <span class="bg-cyan-500 text-slate-900 text-[10px] font-extrabold px-2 py-0.5 rounded uppercase tracking-wider">Pusat Informasi</span>
          <h2 class="text-xl font-extrabold mt-2">Selamat Datang di SAC Garut</h2>
          <p class="text-xs text-slate-300 mt-1 leading-relaxed">Sistem Administrasi & Coaching Renang Professional Kabupaten Garut. Terhubung ke Firestore Cloud Real-time.</p>
        </div>
      </div>

      <!-- Quick Stats Grid -->
      <div class="grid grid-cols-2 gap-3">
        <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
          <p class="text-xs font-semibold text-slate-400">Total Siswa Aktif</p>
          <h3 class="text-2xl font-extrabold text-slate-900 mt-1" x-text="students.length"></h3>
          <p class="text-[10px] text-emerald-600 font-semibold mt-1">Real-time Cloud Sync</p>
        </div>
        <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
          <p class="text-xs font-semibold text-slate-400">Kategori Kelas</p>
          <h3 class="text-2xl font-extrabold text-slate-900 mt-1">4 Level</h3>
          <p class="text-[10px] text-sky-600 font-semibold mt-1">Prestasi hingga Basic</p>
        </div>
      </div>

      <!-- Quick Navigation Cards -->
      <div class="grid grid-cols-2 gap-3">
        <button @click="currentTab = 'siswa'" class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 text-left hover:border-sky-300 transition">
          <div class="w-8 h-8 rounded-lg bg-sky-50 text-sky-600 flex items-center justify-center font-bold mb-2">👥</div>
          <h4 class="font-bold text-sm text-slate-900">Direktori Siswa</h4>
          <p class="text-[11px] text-slate-500 mt-0.5">Lihat 160 data siswa & raport</p>
        </button>
        <button @click="currentTab = 'pelatih'" class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 text-left hover:border-sky-300 transition">
          <div class="w-8 h-8 rounded-lg bg-sky-50 text-sky-600 flex items-center justify-center font-bold mb-2">🏆</div>
          <h4 class="font-bold text-sm text-slate-900">Data Pelatih</h4>
          <p class="text-[11px] text-slate-500 mt-0.5">10 Pelatih Resmi SAC Garut</p>
        </button>
      </div>
    </div>

    <!-- TAB 2: DAFTAR SISWA -->
    <div x-show="currentTab === 'siswa'" class="space-y-3">
      <!-- Search & Filters -->
      <div class="bg-white p-3 rounded-2xl shadow-sm border border-slate-100 space-y-2">
        <input type="text" x-model="searchQuery" placeholder="Cari nama siswa, coach, asal sekolah..." class="w-full px-3.5 py-2 text-xs rounded-xl bg-slate-50 border border-slate-200 focus:outline-none focus:border-sky-500">
        <div class="flex space-x-2 overflow-x-auto pb-1 text-xs">
          <button @click="selectedClass = 'Semua'" :class="selectedClass === 'Semua' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1 rounded-full whitespace-nowrap">Semua</button>
          <button @click="selectedClass = 'Prestasi'" :class="selectedClass === 'Prestasi' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1 rounded-full whitespace-nowrap">Prestasi</button>
          <button @click="selectedClass = 'Lanjutan'" :class="selectedClass === 'Lanjutan' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1 rounded-full whitespace-nowrap">Lanjutan</button>
          <button @click="selectedClass = 'Pemula Moderat'" :class="selectedClass === 'Pemula Moderat' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1 rounded-full whitespace-nowrap">Pemula Moderat</button>
          <button @click="selectedClass = 'Pemula Basic'" :class="selectedClass === 'Pemula Basic' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1 rounded-full whitespace-nowrap">Pemula Basic</button>
        </div>
      </div>

      <!-- Students List -->
      <div class="space-y-2">
        <template x-for="student in filteredStudents" :key="student.id">
          <div class="bg-white p-3.5 rounded-2xl shadow-sm border border-slate-100 flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-full bg-sky-100 text-sky-700 font-bold flex items-center justify-center text-xs" x-text="getInitials(student.namaLengkap)"></div>
              <div>
                <h4 class="font-bold text-xs text-slate-900" x-text="student.namaLengkap"></h4>
                <p class="text-[10px] text-slate-500 mt-0.5"><span class="font-semibold text-sky-600" x-text="student.kelas"></span> • <span x-text="student.hariLatihan"></span></p>
              </div>
            </div>
            <a :href="'https://wa.me/' + cleanPhone(student.noHp)" target="_blank" class="bg-emerald-50 text-emerald-600 px-3 py-1.5 rounded-xl text-[11px] font-bold border border-emerald-200">WhatsApp</a>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB 3: ABSENSI -->
    <div x-show="currentTab === 'absensi'" class="space-y-3">
      <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 text-center py-10">
        <div class="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto text-xl font-bold mb-3">📋</div>
        <h3 class="font-bold text-sm text-slate-900">Modul Absensi & Scan QR</h3>
        <p class="text-xs text-slate-500 mt-1 max-w-xs mx-auto">Gunakan aplikasi Android SAC Garut untuk pemindaian kamera ML Kit Barcode secara real-time.</p>
      </div>
    </div>

    <!-- TAB 4: KEUANGAN -->
    <div x-show="currentTab === 'keuangan'" class="space-y-3">
      <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
        <h3 class="font-bold text-sm text-slate-900">Ringkasan Keuangan Bulanan</h3>
        <p class="text-xs text-slate-500 mt-1">Oktober 2026</p>
        <div class="mt-4 grid grid-cols-2 gap-3">
          <div class="bg-emerald-50 p-3 rounded-xl border border-emerald-100">
            <p class="text-[10px] font-bold text-emerald-700">Total Pemasukan</p>
            <p class="text-base font-extrabold text-emerald-900 mt-1">Rp 24.500.000</p>
          </div>
          <div class="bg-amber-50 p-3 rounded-xl border border-amber-100">
            <p class="text-[10px] font-bold text-amber-700">Belum Lunas</p>
            <p class="text-base font-extrabold text-amber-900 mt-1">Rp 3.200.000</p>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 5: PELATIH -->
    <div x-show="currentTab === 'pelatih'" class="space-y-3">
      <div class="grid grid-cols-2 gap-3">
        <template x-for="coach in coaches" :key="coach.id">
          <div class="bg-white p-3.5 rounded-2xl shadow-sm border border-slate-100">
            <div class="w-9 h-9 rounded-xl bg-sky-500 text-slate-900 font-extrabold flex items-center justify-center text-xs mb-2" x-text="coach.name.substring(6, 8)"></div>
            <h4 class="font-bold text-xs text-slate-900" x-text="coach.name"></h4>
            <p class="text-[10px] text-sky-600 font-semibold mt-0.5" x-text="coach.role"></p>
          </div>
        </template>
      </div>
    </div>
  </main>

  <!-- Bottom Navigation Bar -->
  <nav class="bg-white border-t border-slate-200 fixed bottom-0 left-0 right-0 z-50 shadow-lg">
    <div class="max-w-4xl mx-auto px-4 flex justify-around py-2">
      <button @click="currentTab = 'home'" :class="currentTab === 'home' ? 'text-sky-600' : 'text-slate-400'" class="flex flex-col items-center text-[10px] font-bold">
        <span class="text-lg">🏠</span> Beranda
      </button>
      <button @click="currentTab = 'siswa'" :class="currentTab === 'siswa' ? 'text-sky-600' : 'text-slate-400'" class="flex flex-col items-center text-[10px] font-bold">
        <span class="text-lg">👥</span> Siswa
      </button>
      <button @click="currentTab = 'absensi'" :class="currentTab === 'absensi' ? 'text-sky-600' : 'text-slate-400'" class="flex flex-col items-center text-[10px] font-bold">
        <span class="text-lg">📋</span> Absensi
      </button>
      <button @click="currentTab = 'keuangan'" :class="currentTab === 'keuangan' ? 'text-sky-600' : 'text-slate-400'" class="flex flex-col items-center text-[10px] font-bold">
        <span class="text-lg">💰</span> Keuangan
      </button>
      <button @click="currentTab = 'pelatih'" :class="currentTab === 'pelatih' ? 'text-sky-600' : 'text-slate-400'" class="flex flex-col items-center text-[10px] font-bold">
        <span class="text-lg">🏆</span> Pelatih
      </button>
    </div>
  </nav>

  <script>
    const INITIAL_STUDENTS = {json.dumps(students_data, ensure_ascii=False)};
    const INITIAL_COACHES = {json.dumps(coaches_data, ensure_ascii=False)};

    // Firebase Web SDK Configuration
    const firebaseConfig = {{
      apiKey: "AIzaSyAWJ3DciHh12vEvq9t8xGK4EBm7SIX63ME",
      authDomain: "sac-garut.firebaseapp.com",
      projectId: "sac-garut",
      storageBucket: "sac-garut.firebasestorage.app",
      messagingSenderId: "1025884570496",
      appId: "1:1025884570496:web:e14e28f2ea8ad578c6f765"
    }};

    let db = null;
    if (typeof firebase !== 'undefined') {{
      try {{
        if (!firebase.apps.length) {{
          firebase.initializeApp(firebaseConfig);
        }}
        db = firebase.firestore();
        console.log("Firebase Web SDK initialized for sac-garut");
      }} catch (err) {{
        console.warn("Firebase init notice:", err.message);
      }}
    }}

    function sacApp() {{
      return {{
        currentTab: 'home',
        students: INITIAL_STUDENTS,
        coaches: INITIAL_COACHES,
        searchQuery: '',
        selectedClass: 'Semua',
        init() {{
          if (db) {{
            // Real-time Firestore sync for students
            db.collection("students").onSnapshot((snapshot) => {{
              if (snapshot && !snapshot.empty) {{
                const liveList = [];
                snapshot.forEach(doc => {{
                  liveList.push({{ id: doc.id, ...doc.data() }});
                }});
                if (liveList.length > 0) {{
                  this.students = liveList;
                  console.log("Real-time Firebase students sync:", liveList.length);
                }}
              }}
            }}, (err) => console.warn("Firestore listener info:", err.message));

            // Real-time Firestore sync for coaches
            db.collection("coaches").onSnapshot((snapshot) => {{
              if (snapshot && !snapshot.empty) {{
                const liveCoaches = [];
                snapshot.forEach(doc => {{
                  liveCoaches.push({{ id: doc.id, ...doc.data() }});
                }});
                if (liveCoaches.length > 0) {{
                  this.coaches = liveCoaches;
                }}
              }}
            }}, (err) => console.warn("Firestore coaches info:", err.message));
          }}
        }},
        get filteredStudents() {{
          return this.students.filter(s => {{
            const matchesClass = this.selectedClass === 'Semua' || s.kelas.toLowerCase().includes(this.selectedClass.toLowerCase());
            const matchesQuery = !this.searchQuery || s.namaLengkap.toLowerCase().includes(this.searchQuery.toLowerCase()) || (s.coach && s.coach.toLowerCase().includes(this.searchQuery.toLowerCase()));
            return matchesClass && matchesQuery;
          }});
        }},
        getInitials(name) {{
          const parts = (name || '').trim().split(/\\s+/);
          if (parts.length === 0) return 'SG';
          if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
          return (parts[0][0] + parts[1][0]).toUpperCase();
        }},
        cleanPhone(phone) {{
          let digits = (phone || '').replace(/\\D/g, '');
          if (digits.startsWith('0')) digits = '62' + digits.substring(1);
          return digits;
        }}
      }}
    }}
  </script>
</body>
</html>
"""

os.makedirs("dist", exist_ok=True)
with open("dist/index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print("Successfully generated dist/index.html with Firebase Web SDK & hardcoded fallback!")
