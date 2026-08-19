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

 Date: 08/03/2025 12:14:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dokter_mpp
-- ----------------------------
DROP TABLE IF EXISTS `dokter_mpp`;
CREATE TABLE `dokter_mpp`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `kd_dokter` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
