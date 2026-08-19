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

 Date: 27/03/2025 08:44:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for jawaban_konsultasi_medik
-- ----------------------------
DROP TABLE IF EXISTS `jawaban_konsultasi_medik`;
CREATE TABLE `jawaban_konsultasi_medik`  (
  `no_permintaan` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `tanggal` datetime NOT NULL,
  `diagnosa_kerja` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  `uraian_jawaban` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  PRIMARY KEY (`no_permintaan`) USING BTREE,
  CONSTRAINT `jawaban_konsultasi_medik_ibfk_1` FOREIGN KEY (`no_permintaan`) REFERENCES `konsultasi_medik` (`no_permintaan`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
