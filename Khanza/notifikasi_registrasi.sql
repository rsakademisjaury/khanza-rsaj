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

 Date: 24/04/2025 14:15:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for notifikasi_registrasi
-- ----------------------------
DROP TABLE IF EXISTS `notifikasi_registrasi`;
CREATE TABLE `notifikasi_registrasi`  (
  `id_notifikasi` int NOT NULL AUTO_INCREMENT,
  `no_rkm_medis` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tanggal_booking` date NULL DEFAULT NULL,
  `jam_booking` time NULL DEFAULT NULL,
  `status_notifikasi` enum('Belum Dilihat','Sudah Dilihat') CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'Belum Dilihat',
  `waktu_ditambahkan` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id_notifikasi`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 165 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
