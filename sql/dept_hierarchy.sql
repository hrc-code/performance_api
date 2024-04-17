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

 Date: 29/03/2024 01:20:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dept_hierarchy
-- ----------------------------
DROP TABLE IF EXISTS `dept_hierarchy`;
CREATE TABLE `dept_hierarchy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL COMMENT '父部门ID',
  `child_id` bigint NOT NULL COMMENT '子部门ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dept_hierarchy
-- ----------------------------
INSERT INTO `dept_hierarchy` VALUES (1, 1, 2);
INSERT INTO `dept_hierarchy` VALUES (14, 2, 3);
INSERT INTO `dept_hierarchy` VALUES (15, 2, 4);
INSERT INTO `dept_hierarchy` VALUES (16, 2, 5);
INSERT INTO `dept_hierarchy` VALUES (17, 2, 6);
INSERT INTO `dept_hierarchy` VALUES (18, 2, 7);
INSERT INTO `dept_hierarchy` VALUES (19, 3, 8);
INSERT INTO `dept_hierarchy` VALUES (20, 3, 9);
INSERT INTO `dept_hierarchy` VALUES (21, 4, 10);
INSERT INTO `dept_hierarchy` VALUES (22, 5, 11);
INSERT INTO `dept_hierarchy` VALUES (23, 6, 12);

SET FOREIGN_KEY_CHECKS = 1;
