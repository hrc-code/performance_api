/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 29/03/2024 01:21:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role_btn
-- ----------------------------
DROP TABLE IF EXISTS `role_btn`;
CREATE TABLE `role_btn`  (
  `role_id` bigint NOT NULL COMMENT '角色id',
  `btn_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '按钮id'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_btn
-- ----------------------------
INSERT INTO `role_btn` VALUES (27, '1-1-1');
INSERT INTO `role_btn` VALUES (27, '1-1-2');
INSERT INTO `role_btn` VALUES (27, '1-1-3');
INSERT INTO `role_btn` VALUES (27, '1-1-4');
INSERT INTO `role_btn` VALUES (4, '1-1-1');
INSERT INTO `role_btn` VALUES (4, '1-1-3');
INSERT INTO `role_btn` VALUES (3, '1-1-1');
INSERT INTO `role_btn` VALUES (3, '1-1-2');

SET FOREIGN_KEY_CHECKS = 1;
