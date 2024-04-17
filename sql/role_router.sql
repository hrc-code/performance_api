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

 Date: 29/03/2024 01:22:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role_router
-- ----------------------------
DROP TABLE IF EXISTS `role_router`;
CREATE TABLE `role_router`  (
  `role_id` bigint NOT NULL,
  `router_id` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `state` smallint NOT NULL DEFAULT 1
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态路由表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_router
-- ----------------------------
INSERT INTO `role_router` VALUES (1, '1', 1);
INSERT INTO `role_router` VALUES (1, '1-1', 1);
INSERT INTO `role_router` VALUES (1, '1-2', 1);
INSERT INTO `role_router` VALUES (1, '1-3', 1);
INSERT INTO `role_router` VALUES (1, '1-4', 1);
INSERT INTO `role_router` VALUES (1, '2', 1);
INSERT INTO `role_router` VALUES (1, '2-1', 1);
INSERT INTO `role_router` VALUES (1, '2-2', 1);
INSERT INTO `role_router` VALUES (1, '2-3', 1);
INSERT INTO `role_router` VALUES (1, '2-4', 1);
INSERT INTO `role_router` VALUES (1, '2-5', 1);
INSERT INTO `role_router` VALUES (1, '2-6', 1);
INSERT INTO `role_router` VALUES (1, '2-7', 1);
INSERT INTO `role_router` VALUES (1, '3', 1);
INSERT INTO `role_router` VALUES (1, '3-1', 1);
INSERT INTO `role_router` VALUES (1, '3-2', 1);
INSERT INTO `role_router` VALUES (1, '3-3', 1);
INSERT INTO `role_router` VALUES (1, '4', 1);
INSERT INTO `role_router` VALUES (1, '4-1', 1);
INSERT INTO `role_router` VALUES (1, '4-2', 1);
INSERT INTO `role_router` VALUES (1, '4-3', 1);
INSERT INTO `role_router` VALUES (1, '4-4', 1);
INSERT INTO `role_router` VALUES (1, '4-5', 1);
INSERT INTO `role_router` VALUES (1, '4-6', 1);
INSERT INTO `role_router` VALUES (1, '5', 1);
INSERT INTO `role_router` VALUES (1, '5-1', 1);
INSERT INTO `role_router` VALUES (1, '5-2', 1);
INSERT INTO `role_router` VALUES (1, '5-3', 1);
INSERT INTO `role_router` VALUES (7, '2', 1);
INSERT INTO `role_router` VALUES (7, '2-1', 1);
INSERT INTO `role_router` VALUES (7, '2-2', 1);
INSERT INTO `role_router` VALUES (7, '2-3', 1);
INSERT INTO `role_router` VALUES (7, '3', 1);
INSERT INTO `role_router` VALUES (7, '3-2', 1);
INSERT INTO `role_router` VALUES (7, '5', 1);
INSERT INTO `role_router` VALUES (7, '5-1', 1);
INSERT INTO `role_router` VALUES (7, '5-3', 1);
INSERT INTO `role_router` VALUES (11, '3', 1);
INSERT INTO `role_router` VALUES (11, '3-1', 1);
INSERT INTO `role_router` VALUES (2, '2', 1);
INSERT INTO `role_router` VALUES (2, '2-7', 1);
INSERT INTO `role_router` VALUES (2, '2-8', 1);
INSERT INTO `role_router` VALUES (2, '1', 1);
INSERT INTO `role_router` VALUES (2, '1-3', 1);
INSERT INTO `role_router` VALUES (1, '1-5', 1);
INSERT INTO `role_router` VALUES (1, '6', 1);
INSERT INTO `role_router` VALUES (1, '6-1', 1);
INSERT INTO `role_router` VALUES (1, '6-2', 1);
INSERT INTO `role_router` VALUES (1, '6-3', 1);
INSERT INTO `role_router` VALUES (27, '1', 1);
INSERT INTO `role_router` VALUES (27, '1-1', 1);
INSERT INTO `role_router` VALUES (5, '2', 1);
INSERT INTO `role_router` VALUES (5, '2-1', 1);
INSERT INTO `role_router` VALUES (5, '2-2', 1);
INSERT INTO `role_router` VALUES (5, '2-3', 1);
INSERT INTO `role_router` VALUES (4, '1', 1);
INSERT INTO `role_router` VALUES (4, '1-1', 1);
INSERT INTO `role_router` VALUES (3, '1', 1);
INSERT INTO `role_router` VALUES (3, '1-1', 1);

SET FOREIGN_KEY_CHECKS = 1;
