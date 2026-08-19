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

 Date: 24/04/2025 12:44:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for rsaj_dokter_vclaim
-- ----------------------------
DROP TABLE IF EXISTS `rsaj_dokter_vclaim`;
CREATE TABLE `rsaj_dokter_vclaim`  (
  `kd_dokter` char(10) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '',
  `nm_dokter` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `status` enum('0','1') CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`kd_dokter`) USING BTREE,
  INDEX `nm_poli`(`nm_dokter` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
