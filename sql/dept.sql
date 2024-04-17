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

 Date: 29/03/2024 01:20:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dept
-- ----------------------------
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `level` tinyint UNSIGNED NOT NULL COMMENT '部门级别',
  `state` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新用户',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dept
-- ----------------------------
INSERT INTO `dept` VALUES (1, '中捷总公司', 1, 1, '2024-03-26 20:31:55', '2024-03-26 20:31:55', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (2, '深圳分公司', 2, 1, '2024-03-11 19:36:04', '2024-03-11 19:36:04', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (3, '业务一部', 3, 1, '2024-03-11 19:39:06', '2024-03-11 19:39:06', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (4, '业务二部', 3, 1, '2024-03-11 19:42:12', '2024-03-11 19:42:12', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (5, '业务三部', 3, 1, '2024-03-11 19:42:12', '2024-03-11 19:42:12', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (6, '深圳中心', 3, 1, '2024-03-11 19:42:12', '2024-03-11 19:42:12', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (7, '综合支撑部', 3, 1, '2024-03-11 19:42:12', '2024-03-11 19:42:12', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (8, '信息支撑中心', 4, 1, '2024-03-11 19:44:41', '2024-03-25 16:40:40', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (9, '运营监督室', 4, 1, '2024-03-11 19:44:41', '2024-03-11 19:44:41', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (10, '深圳配送中心', 4, 1, '2024-03-11 19:44:41', '2024-03-11 19:44:41', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (11, '招标业务室', 4, 1, '2024-03-11 19:44:41', '2024-03-11 19:44:41', NULL, NULL, NULL);
INSERT INTO `dept` VALUES (12, '综合支撑室', 4, 1, '2024-03-11 19:44:41', '2024-03-11 19:44:41', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
