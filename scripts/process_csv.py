import csv
import json
import re

csv_path = "public/assets/aistudio/Formulir_Registrasi_Ulang___Evaluasi_SAC_Garut_2026___Responses__cf1436d9"
existing_students_path = "app/src/main/assets/sac_students.json"
output_students_path = "app/src/main/assets/sac_students.json"
output_surveys_path = "app/src/main/assets/sac_parent_surveys.json"

with open(csv_path, "r", encoding="utf-8") as f:
    reader = csv.reader(f)
    header = next(reader)
    rows = list(reader)

with open(existing_students_path, "r", encoding="utf-8") as f:
    existing_students = json.load(f)

print(f"Loaded {len(rows)} CSV rows, {len(existing_students)} existing JSON students.")

coaches_pool = ["Coach Fahmi", "Coach Gilang", "Coach Dani", "Coach Asep", "Coach Sarah", "Coach Bayu"]
time_slots = ["08.00 - 09.30 WIB", "10.00 - 11.30 WIB", "13.30 - 15.00 WIB", "15.30 - 17.00 WIB"]

# Build survey responses
surveys = []
updated_students = []

for idx, r in enumerate(rows):
    row_num = idx + 1
    std_id = f"SAC-2026-{row_num:03d}"
    
    timestamp = r[0].strip() if len(r) > 0 else ""
    nama_lengkap = r[1].strip() if len(r) > 1 else ""
    ttl = r[2].strip() if len(r) > 2 else ""
    asal_sekolah = r[3].strip() if len(r) > 3 else ""
    lama_latihan = r[4].strip() if len(r) > 4 else ""
    tujuan = r[5].strip() if len(r) > 5 else ""
    raw_kelas = r[6].strip() if len(r) > 6 else ""
    hari_latihan = (r[7].strip() if len(r) > 7 else "Minggu").replace("Rabu", "Selasa")
    ukuran_jersey = r[8].strip() if len(r) > 8 else "L"
    nama_ibu = r[9].strip() if len(r) > 9 else ""
    nama_ayah = r[10].strip() if len(r) > 10 else ""
    alamat = r[11].strip() if len(r) > 11 else ""
    no_hp = r[12].strip() if len(r) > 12 else ""
    foto_url = r[13].strip() if len(r) > 13 else ""
    pencapaian = r[14].strip() if len(r) > 14 else ""
    kepuasan_pelatih = r[15].strip() if len(r) > 15 else ""
    penilaian_fasilitas = r[16].strip() if len(r) > 16 else ""
    pendapat_tiket = r[17].strip() if len(r) > 17 else ""
    kritik_saran = r[18].strip() if len(r) > 18 else ""

    # Class normalization
    if "1 Tahun" in lama_latihan or "Prestasi" in lama_latihan or "Prestasi" in raw_kelas:
        normalized_class = "Prestasi (> 1 Tahun)"
    elif "6-12" in lama_latihan or "6 - 12" in lama_latihan or "Moderat" in raw_kelas:
        normalized_class = "Pemula Moderat (6 - 12 Bulan)"
    elif "3-6" in lama_latihan or "3 - 6" in lama_latihan:
        normalized_class = "Pemula (3 - 6 Bulan)"
    else:
        normalized_class = "Pemula Basic (< 3 Bulan)"

    # Parent display name
    parent_parts = []
    if nama_ibu and nama_ibu != "-":
        parent_parts.append(f"Ibu {nama_ibu}")
    if nama_ayah and nama_ayah != "-":
        parent_parts.append(f"Bpk. {nama_ayah}")
    nama_orang_tua = " & ".join(parent_parts) if parent_parts else "Orang Tua Siswa"

    # WhatsApp number
    digits = re.sub(r"\D", "", no_hp)
    if digits.startswith("0"):
        wa_number = "62" + digits[1:]
    elif digits.startswith("62"):
        wa_number = digits
    elif digits:
        wa_number = "62" + digits
    else:
        wa_number = ""

    # Drive photo ID
    drive_match = re.search(r"id=([a-zA-Z0-9_-]+)", foto_url)
    drive_photo_id = drive_match.group(1) if drive_match else ""

    # Check if student existed in previous json
    existing_entry = existing_students[idx] if idx < len(existing_students) else None

    if existing_entry:
        coach = existing_entry.get("coach", coaches_pool[idx % len(coaches_pool)])
        jam_latihan = existing_entry.get("jamLatihan", time_slots[idx % len(time_slots)])
        status_pembayaran = existing_entry.get("statusPembayaran", "Lunas")
        gaya_bebas = existing_entry.get("gayaBebasTime", "")
        gaya_dada = existing_entry.get("gayaDadaTime", "")
        gaya_punggung = existing_entry.get("gayaPunggungTime", "")
        gaya_kupu = existing_entry.get("gayaKupuTime", "")
        attendance_rate = existing_entry.get("attendanceRate", "92%")
        rating_teknik = existing_entry.get("ratingTeknik", 4)
        rating_disiplin = existing_entry.get("ratingDisiplin", 5)
        catatan_raport = existing_entry.get("catatanPelatihRaport", "")
        rekomendasi_level = existing_entry.get("rekomendasiLevel", "")
        tanggal_raport = existing_entry.get("tanggalRaport", "")
    else:
        # Default for new students
        coach = coaches_pool[idx % len(coaches_pool)]
        jam_latihan = time_slots[idx % len(time_slots)]
        status_pembayaran = "Lunas" if (idx % 7 != 0) else "Belum Lunas"
        gaya_bebas = ""
        gaya_dada = ""
        gaya_punggung = ""
        gaya_kupu = ""
        attendance_rate = "90%"
        rating_teknik = 4
        rating_disiplin = 5
        catatan_raport = ""
        rekomendasi_level = ""
        tanggal_raport = ""

    student_obj = {
        "id": std_id,
        "timestamp": timestamp,
        "namaLengkap": nama_lengkap,
        "ttl": ttl,
        "asalSekolah": asal_sekolah,
        "lamaLatihan": lama_latihan,
        "tujuan": tujuan,
        "kelas": normalized_class,
        "hariLatihan": hari_latihan,
        "jamLatihan": jam_latihan,
        "ukuranJersey": ukuran_jersey,
        "namaIbu": nama_ibu,
        "namaAyah": nama_ayah,
        "namaOrangTua": nama_orang_tua,
        "alamat": alamat,
        "noHp": no_hp,
        "waNumber": wa_number,
        "fotoUrl": foto_url,
        "drivePhotoId": drive_photo_id,
        "statusPembayaran": status_pembayaran,
        "coach": coach,
        "pencapaian": pencapaian,
        "kepuasanPelatih": kepuasan_pelatih,
        "penilaianFasilitas": penilaian_fasilitas,
        "pendapatTiket": pendapat_tiket,
        "kritikSaran": kritik_saran,
        "catatanKhusus": "",
        "gayaBebasTime": gaya_bebas,
        "gayaDadaTime": gaya_dada,
        "gayaPunggungTime": gaya_punggung,
        "gayaKupuTime": gaya_kupu,
        "attendanceRate": attendance_rate,
        "ratingTeknik": rating_teknik,
        "ratingDisiplin": rating_disiplin,
        "catatanPelatihRaport": catatan_raport,
        "rekomendasiLevel": rekomendasi_level,
        "tanggalRaport": tanggal_raport
    }
    updated_students.append(student_obj)

    # Survey model object
    survey_obj = {
        "id": f"SRV-2026-{row_num:03d}",
        "namaSiswa": nama_lengkap,
        "namaOrangTua": nama_orang_tua,
        "pencapaian": pencapaian,
        "kepuasanPelatih": kepuasan_pelatih,
        "penilaianFasilitas": penilaian_fasilitas,
        "pendapatTiket": pendapat_tiket,
        "saranKritik": kritik_saran,
        "timestamp": timestamp
    }
    surveys.append(survey_obj)

with open(output_students_path, "w", encoding="utf-8") as f:
    json.dump(updated_students, f, indent=2, ensure_ascii=False)

with open(output_surveys_path, "w", encoding="utf-8") as f:
    json.dump(surveys, f, indent=2, ensure_ascii=False)

print(f"Successfully generated {len(updated_students)} students and {len(surveys)} surveys.")
