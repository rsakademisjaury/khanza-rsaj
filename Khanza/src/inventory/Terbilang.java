/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventory;

/**
 *
 * @author daengmuma
 */
public class Terbilang {
    private static final String[] angkaHuruf = {
        "", "Satu", "Dua", "Tiga", "Empat", "Lima",
        "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"
    };

    public static String indonesian(double nilai) {
        if (nilai == 0) {
            return "nol";
        }

        StringBuilder hasil = new StringBuilder();
        long angkaUtama = (long) nilai;
        hasil.append(ucapan(angkaUtama).trim());

        double pecahan = nilai - angkaUtama;
        if (pecahan > 0) {
            hasil.append(" Koma");
            String pecahanStr = String.valueOf(pecahan).split("\\.")[1];
            for (char c : pecahanStr.toCharArray()) {
                if (Character.isDigit(c)) {
                    hasil.append(" ").append(angkaHuruf[c - '0']);
                }
            }
        }

        return hasil.toString().trim();
    }

    private static String ucapan(long angka) {
        if (angka < 0) {
            return "Minus " + ucapan(-angka);
        }
        if (angka < 12) {
            return angkaHuruf[(int) angka];
        }
        if (angka < 20) {
            return ucapan(angka - 10) + " Belas";
        }
        if (angka < 100) {
            return ucapan(angka / 10) + " Puluh" + ((angka % 10 != 0) ? " " + ucapan(angka % 10) : "");
        }
        if (angka < 200) {
            return "Seratus" + ((angka > 100) ? " " + ucapan(angka - 100) : "");
        }
        if (angka < 1000) {
            return ucapan(angka / 100) + " Ratus" + ((angka % 100 != 0) ? " " + ucapan(angka % 100) : "");
        }
        if (angka < 2000) {
            return "Seribu" + ((angka > 1000) ? " " + ucapan(angka - 1000) : "");
        }
        if (angka < 1000000) {
            return ucapan(angka / 1000) + " Ribu" + ((angka % 1000 != 0) ? " " + ucapan(angka % 1000) : "");
        }
        if (angka < 1000000000) {
            return ucapan(angka / 1000000) + " Juta" + ((angka % 1000000 != 0) ? " " + ucapan(angka % 1000000) : "");
        }
        if (angka < 1000000000000L) {
            return ucapan(angka / 1000000000) + " Milyar" + ((angka % 1000000000 != 0) ? " " + ucapan(angka % 1000000000) : "");
        }
        if (angka < 1000000000000000L) {
            return ucapan(angka / 1000000000000L) + " triliun" + ((angka % 1000000000000L != 0) ? " " + ucapan(angka % 1000000000000L) : "");
        }
        return "Angka Terlalu Besar";
    }
}

