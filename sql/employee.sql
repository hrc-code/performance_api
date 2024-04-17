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

 Date: 29/03/2024 01:17:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `num` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工号',
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '123456' COMMENT '密码',
  `state` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工姓名',
  `phone_num1` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话1',
  `phone_num2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话2',
  `email` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `id_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号码',
  `birthday` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出生年月',
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通信地址',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT 'now()',
  `update_time` datetime NULL DEFAULT 'now()',
  `create_user` bigint NULL DEFAULT NULL,
  `update_user` bigint NULL DEFAULT NULL,
  `role_id` bigint NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_UNIQUE`(`id` ASC) USING BTREE,
  UNIQUE INDEX `id__UNIQUE`(`num` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES (1, 'admin', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-27 11:54:14', '2024-03-27 11:54:02', NULL, NULL, 0);
INSERT INTO `employee` VALUES (2, '10007', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-27 11:53:59', '2024-03-27 11:54:04', NULL, NULL, 0);
INSERT INTO `employee` VALUES (7, '161616', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 21:55:07', '2024-03-20 21:55:07', NULL, NULL, 0);
INSERT INTO `employee` VALUES (8, '151616161', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 21:56:33', '2024-03-20 21:56:33', NULL, NULL, 0);
INSERT INTO `employee` VALUES (10, '15118', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:04:24', '2024-03-20 22:04:24', NULL, NULL, 0);
INSERT INTO `employee` VALUES (12, '46611', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:11:47', '2024-03-20 22:11:47', NULL, NULL, 0);
INSERT INTO `employee` VALUES (13, '46494', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:15:13', '2024-03-20 22:15:13', NULL, NULL, 0);
INSERT INTO `employee` VALUES (14, '1516115', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:21:14', '2024-03-20 22:21:14', NULL, NULL, 0);
INSERT INTO `employee` VALUES (15, '69194', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:25:46', '2024-03-20 22:25:46', NULL, NULL, 0);
INSERT INTO `employee` VALUES (17, '65269', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:27:23', '2024-03-20 22:27:23', NULL, NULL, 0);
INSERT INTO `employee` VALUES (18, '151558', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 22:59:39', '2024-03-20 22:59:39', NULL, NULL, 0);
INSERT INTO `employee` VALUES (19, '2812181', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 23:28:26', '2024-03-20 23:28:26', NULL, NULL, 0);
INSERT INTO `employee` VALUES (20, '21999', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-20 23:31:42', '2024-03-20 23:31:42', NULL, NULL, 0);
INSERT INTO `employee` VALUES (25, '12138', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-22 11:03:34', '2024-03-22 11:03:34', NULL, NULL, 0);
INSERT INTO `employee` VALUES (26, '121389', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-22 11:05:07', '2024-03-22 11:05:07', NULL, NULL, 0);
INSERT INTO `employee` VALUES (27, '165161', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-22 20:07:09', '2024-03-22 20:07:09', NULL, NULL, 0);
INSERT INTO `employee` VALUES (28, '19494', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-22 20:10:23', '2024-03-22 20:10:23', NULL, NULL, 0);
INSERT INTO `employee` VALUES (29, '154661', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-22 21:12:18', '2024-03-22 21:12:18', NULL, NULL, 0);
INSERT INTO `employee` VALUES (30, '16519', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 14:12:26', '2024-03-23 14:12:26', NULL, NULL, 0);
INSERT INTO `employee` VALUES (31, '59494', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 14:14:16', '2024-03-23 14:14:16', NULL, NULL, 0);
INSERT INTO `employee` VALUES (32, '16464', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 15:06:18', '2024-03-23 15:06:18', NULL, NULL, 0);
INSERT INTO `employee` VALUES (33, '119', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 15:10:56', '2024-03-23 15:10:56', NULL, NULL, 0);
INSERT INTO `employee` VALUES (34, '19259', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 15:13:39', '2024-03-23 15:13:39', NULL, NULL, 0);
INSERT INTO `employee` VALUES (35, '261919', '123456', 1, '', '', NULL, '', '', '', NULL, NULL, '2024-03-23 15:15:50', '2024-03-23 15:15:50', NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
