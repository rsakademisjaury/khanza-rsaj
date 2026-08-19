/*
 Navicat Premium Data Transfer

 Source Server         : SIMKESKhanza
 Source Server Type    : MySQL
 Source Server Version : 100419 (10.4.19-MariaDB-log)
 Source Host           : 192.168.10.1:3306
 Source Schema         : sik

 Target Server Type    : MySQL
 Target Server Version : 100419 (10.4.19-MariaDB-log)
 File Encoding         : 65001

 Date: 15/04/2025 10:56:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pasien_meninggal
-- ----------------------------
DROP TABLE IF EXISTS `pasien_meninggal`;
CREATE TABLE `pasien_meninggal`  (
  `no_surat` varchar(25) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '',
  `tanggal` date NULL DEFAULT NULL,
  `jam` time NULL DEFAULT NULL,
  `no_rawat` varchar(17) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `no_rkm_medis` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '',
  `lahir_mati` enum('-','YA','TIDAK') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `doa` enum('-','YA','TIDAK') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `keadaan_meninggal` enum('-','Hamil','Bersalin','Nifas ( masa sampai 2 bulan setelah bersalin / abortus )','Lainnya') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `lainnya` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `diagnosa_utama` varchar(250) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `diagnosa_sekunder` varchar(250) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `penyebab_kematian` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  `penyakit_lain` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  `penyebab_utama_bayi` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `penyebab_lain_bayi` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `penyebab_utama_ibu` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `penyebab_lain_ibu` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `kd_dokter` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `penerima` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `hubungan_pasien` enum('AYAH','IBU','SUAMI','ISTRI','SAUDARA','SEPUPU','ANAK','CUCU','KAKEK','NENEK','PAMAN','BIBI','TEMAN','REKAN KERJA','MERTUA','MENANTU','KEMANAKAN','TETANGGA','DIRI SENDIRI','-') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `tgl_pembuatan` datetime NULL DEFAULT NULL,
  `petugas` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  PRIMARY KEY (`no_rkm_medis`, `no_surat`) USING BTREE,
  INDEX `kd_dokter`(`kd_dokter` ASC) USING BTREE,
  CONSTRAINT `pasien_meninggal_ibfk_1` FOREIGN KEY (`no_rkm_medis`) REFERENCES `pasien` (`no_rkm_medis`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `pasien_meninggal_ibfk_2` FOREIGN KEY (`kd_dokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
