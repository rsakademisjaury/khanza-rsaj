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

 Date: 22/03/2025 03:54:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for konsultasi_medik
-- ----------------------------
DROP TABLE IF EXISTS `konsultasi_medik`;
CREATE TABLE `konsultasi_medik`  (
  `no_permintaan` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `no_rawat` varchar(17) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `tanggal` datetime NOT NULL,
  `jenis_permintaan` enum('Konsultasi','Evaluasi','Rawat Bersama','Alih Rawat','Pre/Post Operasi','Cito Konsul') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `kd_dokter` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `kd_dokter_dikonsuli` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `diagnosa_kerja` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  `uraian_konsultasi` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  PRIMARY KEY (`no_permintaan`) USING BTREE,
  INDEX `no_rawat`(`no_rawat` ASC) USING BTREE,
  INDEX `kd_dokter`(`kd_dokter` ASC) USING BTREE,
  INDEX `kd_dokter_dikonsuli`(`kd_dokter_dikonsuli` ASC) USING BTREE,
  CONSTRAINT `konsultasi_medik_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `konsultasi_medik_ibfk_2` FOREIGN KEY (`kd_dokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `konsultasi_medik_ibfk_3` FOREIGN KEY (`kd_dokter_dikonsuli`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
