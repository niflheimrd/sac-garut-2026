const fs = require('fs');
const path = require('path');

const students = JSON.parse(fs.readFileSync('dist/sac_students.json', 'utf8'));
const coaches = JSON.parse(fs.readFileSync('dist/sac_coaches.json', 'utf8'));

const htmlContent = `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>SAC Garut - Master Data & Evaluasi Renang</title>
  <link rel="manifest" href="manifest.json" />
  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
  <meta name="apple-mobile-web-app-title" content="SAC Garut" />
  <link rel="apple-touch-icon" href="icon-192.png" />
  <meta name="theme-color" content="#0EA5E9" />
  
  <!-- Tailwind CSS & Alpine.js CDNs -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <style>
    /* Base Fallback Styles (in case Tailwind CDN fails or slow network) */
    * { box-sizing: border-box; }
    body { font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    [x-cloak] { display: none !important; }
    
    /* Custom Scrollbars */
    ::-webkit-scrollbar { width: 6px; height: 6px; }
    ::-webkit-scrollbar-track { background: #F1F5F9; }
    ::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 9999px; }
    
    .glass-header { background: rgba(15, 23, 42, 0.92); backdrop-filter: blur(8px); }
    .card-shadow { box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.05), 0 2px 6px -1px rgba(0, 0, 0, 0.02); }
  </style>
</head>
<body x-data="sacApp()" x-init="initApp()" class="min-h-screen flex flex-col pb-24 bg-slate-50">

  <!-- TOP HEADER -->
  <header class="glass-header text-white sticky top-0 z-40 border-b border-slate-800 shadow-lg">
    <div class="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-cyan-400 flex items-center justify-center font-black text-slate-900 text-lg shadow-md border border-cyan-200/40">
          SG
        </div>
        <div>
          <div class="flex items-center space-x-2">
            <h1 class="font-extrabold text-base tracking-tight leading-none text-white">SAC Garut</h1>
            <span class="bg-cyan-500/20 text-cyan-300 text-[10px] font-bold px-2 py-0.5 rounded-full border border-cyan-400/30">v2.0 PWA</span>
          </div>
          <p class="text-[11px] text-slate-300 mt-0.5 font-medium">Master Data & Evaluation System</p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <span class="bg-sky-500/20 text-sky-300 text-xs font-semibold px-2.5 py-1 rounded-full border border-sky-400/30 flex items-center space-x-1">
          <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          <span x-text="students.length + ' Siswa'">160 Siswa</span>
        </span>
      </div>
    </div>
  </header>

  <!-- MAIN CONTAINER -->
  <main class="max-w-4xl mx-auto px-4 py-4 w-full flex-1">
    
    <!-- TAB 1: BERANDA / DASHBOARD -->
    <div x-show="currentTab === 'home'" class="space-y-4">
      <!-- Hero Banner -->
      <div class="bg-gradient-to-br from-slate-900 via-slate-800 to-sky-950 rounded-3xl p-6 text-white card-shadow relative overflow-hidden border border-slate-700/50">
        <div class="absolute -right-8 -bottom-8 w-40 h-40 bg-sky-500/10 rounded-full blur-2xl"></div>
        <div class="relative z-10">
          <div class="inline-flex items-center space-x-1.5 bg-sky-500/20 text-sky-300 text-[11px] font-bold px-3 py-1 rounded-full border border-sky-400/30 mb-2">
            <span>🏊</span>
            <span>Swim Academy Club Garut</span>
          </div>
          <h2 class="text-2xl font-black tracking-tight text-white mt-1">Sistem Informasi Master Data</h2>
          <p class="text-xs text-slate-300 mt-1.5 leading-relaxed max-w-lg">
            Aplikasi resmi kelola data 160 siswa renang, 10 pelatih, 4 kategori kelas, absensi harian, dan evaluasi raport SAC Garut.
          </p>
          <div class="mt-4 flex flex-wrap gap-2 text-xs font-semibold">
            <button @click="currentTab = 'siswa'" class="bg-sky-500 hover:bg-sky-400 text-slate-900 px-4 py-2 rounded-xl font-bold transition shadow-sm flex items-center space-x-1.5">
              <span>👥 Cari Data Siswa</span>
            </button>
            <button @click="currentTab = 'pelatih'" class="bg-slate-800 hover:bg-slate-700 text-slate-200 px-4 py-2 rounded-xl border border-slate-600 transition flex items-center space-x-1.5">
              <span>🏆 Daftar 10 Pelatih</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Quick Stats Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="bg-white p-4 rounded-2xl card-shadow border border-slate-100 flex flex-col justify-between">
          <div>
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Siswa</span>
            <h3 class="text-2xl font-black text-slate-900 mt-1" x-text="students.length">160</h3>
          </div>
          <p class="text-[10px] text-emerald-600 font-bold mt-2 flex items-center">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 mr-1"></span> 100% Data Aktif
          </p>
        </div>

        <div class="bg-white p-4 rounded-2xl card-shadow border border-slate-100 flex flex-col justify-between">
          <div>
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Pelatih Resmi</span>
            <h3 class="text-2xl font-black text-sky-600 mt-1" x-text="coaches.length">10</h3>
          </div>
          <p class="text-[10px] text-sky-600 font-bold mt-2">Coach Fatur dkk.</p>
        </div>

        <div class="bg-white p-4 rounded-2xl card-shadow border border-slate-100 flex flex-col justify-between">
          <div>
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Level Kelas</span>
            <h3 class="text-2xl font-black text-amber-500 mt-1">4 Level</h3>
          </div>
          <p class="text-[10px] text-slate-500 font-semibold mt-2">Prestasi - Basic</p>
        </div>

        <div class="bg-white p-4 rounded-2xl card-shadow border border-slate-100 flex flex-col justify-between">
          <div>
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Status Sync</span>
            <h3 class="text-xl font-black text-emerald-600 mt-1">Real-time</h3>
          </div>
          <p class="text-[10px] text-emerald-700 font-bold mt-2">Terhubung Firebase</p>
        </div>
      </div>

      <!-- Class Categories Breakdown -->
      <div class="bg-white p-5 rounded-3xl card-shadow border border-slate-100 space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="font-bold text-sm text-slate-900 flex items-center space-x-2">
            <span>📊</span> <span>Sebaran Siswa Per Kategori Kelas</span>
          </h3>
          <span class="text-xs text-sky-600 font-bold">4 Kategori</span>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <div @click="selectedClass = 'Prestasi'; currentTab = 'siswa'" class="p-3.5 rounded-2xl bg-amber-50 border border-amber-200/60 cursor-pointer hover:bg-amber-100/60 transition flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-9 h-9 rounded-xl bg-amber-500 text-white font-black flex items-center justify-center text-sm shadow-sm">👑</div>
              <div>
                <h4 class="font-bold text-xs text-amber-950">Prestasi</h4>
                <p class="text-[10px] text-amber-700 font-semibold">Pembinaan Atlet & Lomba</p>
              </div>
            </div>
            <span class="text-sm font-black text-amber-900 bg-white px-2.5 py-1 rounded-xl shadow-xs" x-text="getClassCount('Prestasi') + ' Siswa'"></span>
          </div>

          <div @click="selectedClass = 'Lanjutan'; currentTab = 'siswa'" class="p-3.5 rounded-2xl bg-sky-50 border border-sky-200/60 cursor-pointer hover:bg-sky-100/60 transition flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-9 h-9 rounded-xl bg-sky-500 text-white font-black flex items-center justify-center text-sm shadow-sm">🐬</div>
              <div>
                <h4 class="font-bold text-xs text-sky-950">Lanjutan</h4>
                <p class="text-[10px] text-sky-700 font-semibold">Pengembangan Teknik Gaya</p>
              </div>
            </div>
            <span class="text-sm font-black text-sky-900 bg-white px-2.5 py-1 rounded-xl shadow-xs" x-text="getClassCount('Lanjutan') + ' Siswa'"></span>
          </div>

          <div @click="selectedClass = 'Pemula Moderat'; currentTab = 'siswa'" class="p-3.5 rounded-2xl bg-teal-50 border border-teal-200/60 cursor-pointer hover:bg-teal-100/60 transition flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-9 h-9 rounded-xl bg-teal-500 text-white font-black flex items-center justify-center text-sm shadow-sm">🏊</div>
              <div>
                <h4 class="font-bold text-xs text-teal-950">Pemula Moderat</h4>
                <p class="text-[10px] text-teal-700 font-semibold">Pemantapan Water Trappen</p>
              </div>
            </div>
            <span class="text-sm font-black text-teal-900 bg-white px-2.5 py-1 rounded-xl shadow-xs" x-text="getClassCount('Pemula Moderat') + ' Siswa'"></span>
          </div>

          <div @click="selectedClass = 'Pemula Basic'; currentTab = 'siswa'" class="p-3.5 rounded-2xl bg-indigo-50 border border-indigo-200/60 cursor-pointer hover:bg-indigo-100/60 transition flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-9 h-9 rounded-xl bg-indigo-500 text-white font-black flex items-center justify-center text-sm shadow-sm">🐣</div>
              <div>
                <h4 class="font-bold text-xs text-indigo-950">Pemula Basic</h4>
                <p class="text-[10px] text-indigo-700 font-semibold">Pengenalan Air & Basic Floating</p>
              </div>
            </div>
            <span class="text-sm font-black text-indigo-900 bg-white px-2.5 py-1 rounded-xl shadow-xs" x-text="getClassCount('Pemula Basic') + ' Siswa'"></span>
          </div>
        </div>
      </div>

      <!-- Days Schedule Summary -->
      <div class="bg-white p-5 rounded-3xl card-shadow border border-slate-100 space-y-3">
        <h3 class="font-bold text-sm text-slate-900 flex items-center space-x-2">
          <span>📅</span> <span>Jadwal Hari Latihan Siswa</span>
        </h3>
        <div class="grid grid-cols-2 sm:grid-cols-5 gap-2 text-center text-xs">
          <button @click="selectedDay = 'Selasa'; currentTab = 'siswa'" class="p-3 rounded-2xl bg-slate-50 border border-slate-200 hover:border-sky-400 transition">
            <span class="block font-bold text-slate-900">Selasa</span>
            <span class="text-[10px] text-sky-600 font-bold" x-text="getDayCount('Selasa') + ' Siswa'"></span>
          </button>
          <button @click="selectedDay = 'Kamis'; currentTab = 'siswa'" class="p-3 rounded-2xl bg-slate-50 border border-slate-200 hover:border-sky-400 transition">
            <span class="block font-bold text-slate-900">Kamis</span>
            <span class="text-[10px] text-sky-600 font-bold" x-text="getDayCount('Kamis') + ' Siswa'"></span>
          </button>
          <button @click="selectedDay = 'Jum\'at'; currentTab = 'siswa'" class="p-3 rounded-2xl bg-slate-50 border border-slate-200 hover:border-sky-400 transition">
            <span class="block font-bold text-slate-900">Jumat</span>
            <span class="text-[10px] text-sky-600 font-bold" x-text="getDayCount('Jum\'at') + ' Siswa'"></span>
          </button>
          <button @click="selectedDay = 'Sabtu'; currentTab = 'siswa'" class="p-3 rounded-2xl bg-slate-50 border border-slate-200 hover:border-sky-400 transition">
            <span class="block font-bold text-slate-900">Sabtu</span>
            <span class="text-[10px] text-sky-600 font-bold" x-text="getDayCount('Sabtu') + ' Siswa'"></span>
          </button>
          <button @click="selectedDay = 'Minggu'; currentTab = 'siswa'" class="p-3 rounded-2xl bg-slate-50 border border-slate-200 hover:border-sky-400 transition col-span-2 sm:col-span-1">
            <span class="block font-bold text-slate-900">Minggu</span>
            <span class="text-[10px] text-sky-600 font-bold" x-text="getDayCount('Minggu') + ' Siswa'"></span>
          </button>
        </div>
      </div>
    </div>

    <!-- TAB 2: DAFTAR SISWA (160 Data) -->
    <div x-show="currentTab === 'siswa'" class="space-y-3">
      <!-- Search & Filters -->
      <div class="bg-white p-4 rounded-3xl card-shadow border border-slate-100 space-y-3 sticky top-16 z-30">
        <!-- Search Input -->
        <div class="relative">
          <input type="text" x-model="searchQuery" placeholder="Cari nama siswa, orang tua, sekolah, coach..." class="w-full pl-10 pr-4 py-2.5 text-xs rounded-2xl bg-slate-50 border border-slate-200 focus:outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-200 font-medium">
          <span class="absolute left-3.5 top-2.5 text-slate-400 text-sm">🔍</span>
          <button x-show="searchQuery" @click="searchQuery = ''" class="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600 text-xs font-bold bg-slate-200 rounded-full w-4 h-4 flex items-center justify-center">×</button>
        </div>

        <!-- Class Filter Pill Buttons -->
        <div class="flex space-x-1.5 overflow-x-auto pb-1 text-xs no-scrollbar">
          <button @click="selectedClass = 'Semua'" :class="selectedClass === 'Semua' ? 'bg-slate-900 text-white font-extrabold shadow-sm' : 'bg-slate-100 text-slate-600 font-medium hover:bg-slate-200'" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition">Semua Kelas</button>
          <button @click="selectedClass = 'Prestasi'" :class="selectedClass === 'Prestasi' ? 'bg-amber-500 text-white font-extrabold shadow-sm' : 'bg-amber-50 text-amber-800 font-semibold hover:bg-amber-100'" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition">Prestasi</button>
          <button @click="selectedClass = 'Lanjutan'" :class="selectedClass === 'Lanjutan' ? 'bg-sky-500 text-white font-extrabold shadow-sm' : 'bg-sky-50 text-sky-800 font-semibold hover:bg-sky-100'" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition">Lanjutan</button>
          <button @click="selectedClass = 'Pemula Moderat'" :class="selectedClass === 'Pemula Moderat' ? 'bg-teal-500 text-white font-extrabold shadow-sm' : 'bg-teal-50 text-teal-800 font-semibold hover:bg-teal-100'" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition">Pemula Moderat</button>
          <button @click="selectedClass = 'Pemula Basic'" :class="selectedClass === 'Pemula Basic' ? 'bg-indigo-500 text-white font-extrabold shadow-sm' : 'bg-indigo-50 text-indigo-800 font-semibold hover:bg-indigo-100'" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition">Pemula Basic</button>
        </div>

        <!-- Day Filter Pill Buttons -->
        <div class="flex items-center space-x-1.5 overflow-x-auto pb-0.5 text-[11px] no-scrollbar border-t border-slate-100 pt-2">
          <span class="text-slate-400 font-semibold whitespace-nowrap mr-1">Hari:</span>
          <button @click="selectedDay = 'Semua'" :class="selectedDay === 'Semua' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Semua</button>
          <button @click="selectedDay = 'Selasa'" :class="selectedDay === 'Selasa' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Selasa</button>
          <button @click="selectedDay = 'Kamis'" :class="selectedDay === 'Kamis' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Kamis</button>
          <button @click="selectedDay = 'Jum\'at'" :class="selectedDay === 'Jum\'at' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Jumat</button>
          <button @click="selectedDay = 'Sabtu'" :class="selectedDay === 'Sabtu' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Sabtu</button>
          <button @click="selectedDay = 'Minggu'" :class="selectedDay === 'Minggu' ? 'bg-sky-600 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-2.5 py-1 rounded-lg whitespace-nowrap">Minggu</button>
        </div>

        <div class="flex items-center justify-between text-[11px] text-slate-500 font-medium px-1">
          <span>Menampilkan <strong x-text="filteredStudents.length"></strong> siswa</span>
          <button x-show="selectedClass !== 'Semua' || selectedDay !== 'Semua' || searchQuery" @click="resetFilters()" class="text-sky-600 font-bold hover:underline">Reset Filter</button>
        </div>
      </div>

      <!-- Students List Items -->
      <div class="space-y-2.5">
        <template x-for="student in filteredStudents" :key="student.id">
          <div class="bg-white p-4 rounded-3xl card-shadow border border-slate-100 hover:border-sky-300 transition flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div class="flex items-start space-x-3.5">
              <!-- Avatar Circle -->
              <div class="w-11 h-11 rounded-2xl bg-gradient-to-tr from-sky-500 to-cyan-400 text-slate-900 font-black flex items-center justify-center text-xs shadow-sm flex-shrink-0" x-text="getInitials(student.namaLengkap)"></div>
              
              <div class="space-y-0.5">
                <div class="flex items-center space-x-2">
                  <h4 class="font-extrabold text-xs sm:text-sm text-slate-900 leading-tight" x-text="student.namaLengkap"></h4>
                  <span class="text-[10px] font-extrabold px-2 py-0.5 rounded-full" :class="getClassBadgeStyle(student.kelas)" x-text="student.kelas"></span>
                </div>

                <p class="text-[11px] text-slate-500 font-medium">
                  <span class="text-slate-700 font-semibold" x-text="student.asalSekolah"></span> • Hari <span class="text-sky-600 font-bold" x-text="student.hariLatihan"></span>
                </p>

                <p class="text-[10px] text-slate-400">
                  Ortu: <span x-text="student.namaIbu || student.namaAyah || 'Terdaftar'"></span> • Coach: <span class="font-bold text-slate-600" x-text="student.coach"></span>
                </p>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center space-x-2 justify-end pt-2 sm:pt-0 border-t sm:border-t-0 border-slate-100">
              <button @click="openDetailModal(student)" class="bg-slate-100 hover:bg-slate-200 text-slate-800 px-3.5 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-1">
                <span>📄 Detail Raport</span>
              </button>
              <a :href="'https://wa.me/' + cleanPhone(student.noHp)" target="_blank" class="bg-emerald-500 hover:bg-emerald-600 text-white px-3.5 py-2 rounded-xl text-xs font-extrabold transition flex items-center space-x-1 shadow-sm">
                <span>💬 WA</span>
              </a>
            </div>
          </div>
        </template>

        <!-- Empty Filter Result -->
        <div x-show="filteredStudents.length === 0" class="bg-white p-8 rounded-3xl card-shadow border border-slate-100 text-center space-y-2">
          <div class="text-3xl">🔍</div>
          <h4 class="font-bold text-sm text-slate-900">Tidak ada data siswa ditemukan</h4>
          <p class="text-xs text-slate-500">Coba ubah kata kunci pencarian atau reset filter kelas/hari.</p>
          <button @click="resetFilters()" class="mt-2 bg-sky-500 text-slate-900 px-4 py-2 rounded-xl text-xs font-bold">Reset Filter</button>
        </div>
      </div>
    </div>

    <!-- TAB 3: DAFTAR 10 PELATIH -->
    <div x-show="currentTab === 'pelatih'" class="space-y-4">
      <div class="bg-slate-900 text-white p-5 rounded-3xl card-shadow flex items-center justify-between">
        <div>
          <span class="bg-cyan-500/20 text-cyan-300 text-[10px] font-bold px-2 py-0.5 rounded-full border border-cyan-400/30">Official Staff</span>
          <h3 class="text-lg font-black mt-1">Tim Pelatih SAC Garut</h3>
          <p class="text-xs text-slate-300 mt-0.5">10 Coach Profesional Bersertifikat</p>
        </div>
        <div class="w-12 h-12 rounded-2xl bg-cyan-500 text-slate-900 font-black flex items-center justify-center text-xl shadow-md">
          🏆
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <template x-for="coach in coaches" :key="coach.id">
          <div class="bg-white p-4 rounded-3xl card-shadow border border-slate-100 hover:border-sky-300 transition flex items-start space-x-3.5">
            <div class="w-12 h-12 rounded-2xl bg-gradient-to-tr from-sky-500 to-cyan-400 text-slate-900 font-black flex items-center justify-center text-sm shadow-sm flex-shrink-0" x-text="coach.name.replace('Coach ', '').substring(0, 2).toUpperCase()"></div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <h4 class="font-extrabold text-sm text-slate-900" x-text="coach.name"></h4>
                <span class="bg-sky-50 text-sky-700 text-[10px] font-bold px-2 py-0.5 rounded-full" x-text="coach.id"></span>
              </div>
              <p class="text-xs font-bold text-sky-600 mt-0.5" x-text="coach.role"></p>
              <p class="text-[11px] text-slate-500 mt-1" x-text="coach.bio"></p>
              <div class="mt-3 flex items-center justify-between pt-2 border-t border-slate-100 text-[11px]">
                <span class="text-slate-400 font-medium">Pembimbing Siswa</span>
                <a :href="'https://wa.me/' + cleanPhone(coach.phone)" target="_blank" class="text-emerald-600 font-bold hover:underline flex items-center space-x-1">
                  <span>Hubungi Coach</span>
                </a>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB 4: ABSENSI HARIAN -->
    <div x-show="currentTab === 'absensi'" class="space-y-4">
      <div class="bg-white p-5 rounded-3xl card-shadow border border-slate-100 space-y-3">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-extrabold text-base text-slate-900">Absensi & Check-in Kelas</h3>
            <p class="text-xs text-slate-500">Pilih hari untuk memfilter jadwal siswa</p>
          </div>
          <span class="bg-emerald-100 text-emerald-800 text-xs font-bold px-3 py-1 rounded-full" x-text="todayDay"></span>
        </div>

        <div class="flex space-x-2 overflow-x-auto pb-1 text-xs no-scrollbar">
          <button @click="attendanceDay = 'Selasa'" :class="attendanceDay === 'Selasa' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1.5 rounded-xl">Selasa</button>
          <button @click="attendanceDay = 'Kamis'" :class="attendanceDay === 'Kamis' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1.5 rounded-xl">Kamis</button>
          <button @click="attendanceDay = 'Jum\'at'" :class="attendanceDay === 'Jum\'at' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1.5 rounded-xl">Jumat</button>
          <button @click="attendanceDay = 'Sabtu'" :class="attendanceDay === 'Sabtu' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1.5 rounded-xl">Sabtu</button>
          <button @click="attendanceDay = 'Minggu'" :class="attendanceDay === 'Minggu' ? 'bg-slate-900 text-white font-bold' : 'bg-slate-100 text-slate-600'" class="px-3 py-1.5 rounded-xl">Minggu</button>
        </div>
      </div>

      <!-- Scheduled Students for Attendance -->
      <div class="space-y-2">
        <template x-for="student in attendanceStudents" :key="student.id">
          <div class="bg-white p-3.5 rounded-2xl card-shadow border border-slate-100 flex items-center justify-between">
            <div>
              <h4 class="font-bold text-xs text-slate-900" x-text="student.namaLengkap"></h4>
              <p class="text-[10px] text-slate-500"><span class="font-semibold text-sky-600" x-text="student.kelas"></span> • Jam <span x-text="student.jamLatihan"></span></p>
            </div>
            <button @click="toggleAttendance(student.id)" :class="isAttended(student.id) ? 'bg-emerald-500 text-white font-bold' : 'bg-slate-100 text-slate-600 font-semibold'" class="px-3.5 py-1.5 rounded-xl text-xs transition">
              <span x-text="isAttended(student.id) ? '✓ Hadir' : '+ Absen'"></span>
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB 5: EXPORT & DATA BACKUP -->
    <div x-show="currentTab === 'export'" class="space-y-4">
      <div class="bg-white p-6 rounded-3xl card-shadow border border-slate-100 text-center space-y-3">
        <div class="w-14 h-14 bg-sky-50 text-sky-600 rounded-2xl flex items-center justify-center mx-auto text-2xl font-bold">
          📥
        </div>
        <h3 class="font-black text-base text-slate-900">Unduh & Export Master Data</h3>
        <p class="text-xs text-slate-500 max-w-sm mx-auto leading-relaxed">
          Export seluruh 160 data siswa SAC Garut ke dalam format CSV Excel atau JSON untuk keperluan arsip offline.
        </p>

        <div class="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
          <button @click="downloadCSV()" class="w-full sm:w-auto bg-emerald-500 hover:bg-emerald-600 text-white px-5 py-2.5 rounded-2xl text-xs font-extrabold shadow-sm transition flex items-center justify-center space-x-2">
            <span>📄 Download Excel CSV (160 Siswa)</span>
          </button>
          <button @click="downloadJSON()" class="w-full sm:w-auto bg-sky-500 hover:bg-sky-600 text-slate-900 px-5 py-2.5 rounded-2xl text-xs font-extrabold shadow-sm transition flex items-center justify-center space-x-2">
            <span>📦 Download sac_students.json</span>
          </button>
        </div>
      </div>
    </div>

  </main>

  <!-- DETAIL STUDENT MODAL POPUP -->
  <div x-show="activeStudentModal" x-cloak class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs" @click.self="activeStudentModal = null">
    <div class="bg-white w-full max-w-lg rounded-3xl card-shadow overflow-hidden flex flex-col max-h-[90vh]">
      <!-- Modal Header -->
      <div class="bg-gradient-to-r from-slate-900 to-sky-950 p-5 text-white flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-xl bg-cyan-400 text-slate-900 font-black flex items-center justify-center text-xs shadow-inner" x-text="getInitials(activeStudentModal?.namaLengkap || '')"></div>
          <div>
            <h3 class="font-black text-sm text-white" x-text="activeStudentModal?.namaLengkap"></h3>
            <p class="text-[10px] text-cyan-300 font-medium" x-text="activeStudentModal?.id"></p>
          </div>
        </div>
        <button @click="activeStudentModal = null" class="w-8 h-8 rounded-full bg-white/10 text-white hover:bg-white/20 flex items-center justify-center font-bold text-sm">×</button>
      </div>

      <!-- Modal Body (Scrollable) -->
      <div class="p-5 overflow-y-auto space-y-4 text-xs">
        <div class="grid grid-cols-2 gap-3 bg-slate-50 p-3.5 rounded-2xl border border-slate-100">
          <div>
            <span class="text-[10px] font-bold text-slate-400 uppercase">Kategori Kelas</span>
            <p class="font-extrabold text-sky-700 mt-0.5" x-text="activeStudentModal?.kelas"></p>
          </div>
          <div>
            <span class="text-[10px] font-bold text-slate-400 uppercase">Hari Latihan</span>
            <p class="font-extrabold text-slate-900 mt-0.5" x-text="activeStudentModal?.hariLatihan"></p>
          </div>
        </div>

        <div class="space-y-2">
          <h4 class="font-extrabold text-slate-900 text-xs border-b border-slate-100 pb-1">Identitas Siswa</h4>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-slate-600">
            <p><strong>Tempat/Tgl Lahir:</strong> <span x-text="activeStudentModal?.ttl"></span></p>
            <p><strong>Asal Sekolah:</strong> <span x-text="activeStudentModal?.asalSekolah"></span></p>
            <p><strong>Lama Les:</strong> <span x-text="activeStudentModal?.lamaLatihan"></span></p>
            <p><strong>Ukuran Jersey:</strong> <span x-text="activeStudentModal?.ukuranJersey"></span></p>
          </div>
        </div>

        <div class="space-y-2">
          <h4 class="font-extrabold text-slate-900 text-xs border-b border-slate-100 pb-1">Kontak Orang Tua & Alamat</h4>
          <div class="space-y-1 text-slate-600">
            <p><strong>Orang Tua:</strong> <span x-text="activeStudentModal?.namaOrangTua || (activeStudentModal?.namaIbu + ' / ' + activeStudentModal?.namaAyah)"></span></p>
            <p><strong>No. HP / WA:</strong> <span x-text="activeStudentModal?.noHp"></span></p>
            <p><strong>Alamat:</strong> <span x-text="activeStudentModal?.alamat"></span></p>
          </div>
        </div>

        <div class="space-y-2">
          <h4 class="font-extrabold text-slate-900 text-xs border-b border-slate-100 pb-1">Status Pembinaan & Raport</h4>
          <div class="grid grid-cols-2 gap-2 bg-sky-50 p-3 rounded-2xl text-sky-950">
            <p><strong>Pembayaran:</strong> <span class="font-bold text-emerald-700" x-text="activeStudentModal?.statusPembayaran"></span></p>
            <p><strong>Target:</strong> <span class="font-bold text-sky-800" x-text="activeStudentModal?.tujuan"></span></p>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
        <a :href="'https://wa.me/' + cleanPhone(activeStudentModal?.noHp || '')" target="_blank" class="bg-emerald-500 text-white px-4 py-2 rounded-xl text-xs font-extrabold hover:bg-emerald-600 transition flex items-center space-x-1">
          <span>💬 Chat Orang Tua (WA)</span>
        </a>
        <button @click="activeStudentModal = null" class="bg-slate-200 text-slate-700 px-4 py-2 rounded-xl text-xs font-bold hover:bg-slate-300">Tutup</button>
      </div>
    </div>
  </div>

  <!-- BOTTOM NAVIGATION BAR -->
  <nav class="bg-white/95 backdrop-blur-md border-t border-slate-200 fixed bottom-0 left-0 right-0 z-40 shadow-xl">
    <div class="max-w-4xl mx-auto px-4 flex justify-around py-2 text-[11px] font-extrabold">
      <button @click="currentTab = 'home'" :class="currentTab === 'home' ? 'text-sky-600' : 'text-slate-400 hover:text-slate-600'" class="flex flex-col items-center space-y-0.5 py-1">
        <span class="text-lg leading-none">🏠</span>
        <span>Beranda</span>
      </button>
      <button @click="currentTab = 'siswa'" :class="currentTab === 'siswa' ? 'text-sky-600' : 'text-slate-400 hover:text-slate-600'" class="flex flex-col items-center space-y-0.5 py-1">
        <span class="text-lg leading-none">👥</span>
        <span>Siswa (160)</span>
      </button>
      <button @click="currentTab = 'pelatih'" :class="currentTab === 'pelatih' ? 'text-sky-600' : 'text-slate-400 hover:text-slate-600'" class="flex flex-col items-center space-y-0.5 py-1">
        <span class="text-lg leading-none">🏆</span>
        <span>Pelatih (10)</span>
      </button>
      <button @click="currentTab = 'absensi'" :class="currentTab === 'absensi' ? 'text-sky-600' : 'text-slate-400 hover:text-slate-600'" class="flex flex-col items-center space-y-0.5 py-1">
        <span class="text-lg leading-none">📋</span>
        <span>Absensi</span>
      </button>
      <button @click="currentTab = 'export'" :class="currentTab === 'export' ? 'text-sky-600' : 'text-slate-400 hover:text-slate-600'" class="flex flex-col items-center space-y-0.5 py-1">
        <span class="text-lg leading-none">📥</span>
        <span>Export</span>
      </button>
    </div>
  </nav>

  <!-- INLINE DATASET & JS LOGIC -->
  <script>
    const INITIAL_STUDENTS = ${JSON.stringify(students)};
    const INITIAL_COACHES = ${JSON.stringify(coaches)};

    function sacApp() {
      return {
        currentTab: 'home',
        students: INITIAL_STUDENTS,
        coaches: INITIAL_COACHES,
        searchQuery: '',
        selectedClass: 'Semua',
        selectedDay: 'Semua',
        attendanceDay: 'Selasa',
        attendedMap: {},
        activeStudentModal: null,
        todayDay: 'Selasa',

        initApp() {
          console.log('SAC Garut App initialized with', this.students.length, 'students and', this.coaches.length, 'coaches.');
          // Try fetching updated JSON if available over HTTP/S, otherwise fallback safely to embedded dataset
          if (window.location.protocol.startsWith('http')) {
            Promise.all([
              fetch('sac_students.json').then(r => r.ok ? r.json() : null).catch(() => null),
              fetch('sac_coaches.json').then(r => r.ok ? r.json() : null).catch(() => null)
            ]).then(([st, ch]) => {
              if (st && st.length) this.students = st;
              if (ch && ch.length) this.coaches = ch;
            });
          }
        },

        get filteredStudents() {
          return this.students.filter(s => {
            const matchesClass = this.selectedClass === 'Semua' || s.kelas.toLowerCase().includes(this.selectedClass.toLowerCase());
            const matchesDay = this.selectedDay === 'Semua' || (s.hariLatihan && s.hariLatihan.toLowerCase().includes(this.selectedDay.toLowerCase()));
            const q = this.searchQuery.toLowerCase();
            const matchesQuery = !q || 
              (s.namaLengkap && s.namaLengkap.toLowerCase().includes(q)) ||
              (s.asalSekolah && s.asalSekolah.toLowerCase().includes(q)) ||
              (s.coach && s.coach.toLowerCase().includes(q)) ||
              (s.namaIbu && s.namaIbu.toLowerCase().includes(q)) ||
              (s.namaAyah && s.namaAyah.toLowerCase().includes(q));
            return matchesClass && matchesDay && matchesQuery;
          });
        },

        get attendanceStudents() {
          return this.students.filter(s => s.hariLatihan && s.hariLatihan.toLowerCase().includes(this.attendanceDay.toLowerCase()));
        },

        getClassCount(cls) {
          return this.students.filter(s => s.kelas.toLowerCase().includes(cls.toLowerCase())).length;
        },

        getDayCount(day) {
          return this.students.filter(s => s.hariLatihan && s.hariLatihan.toLowerCase().includes(day.toLowerCase())).length;
        },

        resetFilters() {
          this.searchQuery = '';
          this.selectedClass = 'Semua';
          this.selectedDay = 'Semua';
        },

        getInitials(name) {
          if (!name) return 'SG';
          const parts = name.trim().split(/\\s+/);
          if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
          return (parts[0][0] + parts[1][0]).toUpperCase();
        },

        cleanPhone(phone) {
          if (!phone) return '';
          let digits = String(phone).replace(/\\D/g, '');
          if (digits.startsWith('0')) digits = '62' + digits.substring(1);
          return digits;
        },

        getClassBadgeStyle(cls) {
          if (!cls) return 'bg-slate-100 text-slate-700';
          const c = cls.toLowerCase();
          if (c.includes('prestasi')) return 'bg-amber-100 text-amber-800 border border-amber-300';
          if (c.includes('lanjutan')) return 'bg-sky-100 text-sky-800 border border-sky-300';
          if (c.includes('moderat')) return 'bg-teal-100 text-teal-800 border border-teal-300';
          return 'bg-indigo-100 text-indigo-800 border border-indigo-300';
        },

        openDetailModal(student) {
          this.activeStudentModal = student;
        },

        toggleAttendance(id) {
          this.attendedMap[id] = !this.attendedMap[id];
        },

        isAttended(id) {
          return !!this.attendedMap[id];
        },

        downloadCSV() {
          let csv = 'ID,Nama Lengkap,Tempat Tanggal Lahir,Asal Sekolah,Kategori Kelas,Hari Latihan,Nama Orang Tua,No HP,Alamat,Coach\\n';
          this.students.forEach(s => {
            csv += \`"\${s.id}","\${s.namaLengkap}","\${s.ttl}","\${s.asalSekolah}","\${s.kelas}","\${s.hariLatihan}","\${s.namaOrangTua || s.namaIbu}","\${s.noHp}","\${s.alamat}","\${s.coach}"\\n\`;
          });
          const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'SAC_Garut_Master_Siswa_160.csv';
          a.click();
        },

        downloadJSON() {
          const blob = new Blob([JSON.stringify(this.students, null, 2)], { type: 'application/json' });
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'sac_students.json';
          a.click();
        }
      };
    }
  </script>
</body>
</html>
`;

fs.writeFileSync('dist/index.html', htmlContent);
fs.writeFileSync('public/index.html', htmlContent);
console.log('Successfully generated dist/index.html and public/index.html with embedded dataset!');
