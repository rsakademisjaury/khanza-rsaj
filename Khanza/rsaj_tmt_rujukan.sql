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

 Date: 24/04/2025 12:44:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for rsaj_tmt_rujukan
-- ----------------------------
DROP TABLE IF EXISTS `rsaj_tmt_rujukan`;
CREATE TABLE `rsaj_tmt_rujukan`  (
  `no_rawat` varchar(17) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `awal_rujukan` date NULL DEFAULT NULL,
  `akhir_rujukan` date NULL DEFAULT NULL,
  `petugas` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `waktu` datetime NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`no_rawat`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
