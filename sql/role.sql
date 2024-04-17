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

 Date: 29/03/2024 01:21:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `state` smallint NOT NULL COMMENT '角色状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新用户',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建用户',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_name`(`role_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '管理员', 1, '2024-03-05 15:53:30', '2024-03-05 15:53:31', NULL, NULL, NULL);
INSERT INTO `role` VALUES (2, '总经理', 1, '2024-03-05 16:00:04', '2024-03-05 16:00:05', NULL, NULL, NULL);
INSERT INTO `role` VALUES (3, '副总经理', 1, '2024-03-05 16:00:31', '2024-03-19 20:24:31', NULL, NULL, NULL);
INSERT INTO `role` VALUES (4, '总助', 1, '2024-03-05 16:00:45', '2024-03-18 22:07:44', NULL, NULL, NULL);
INSERT INTO `role` VALUES (5, '三级CEO正', 1, '2024-03-05 16:01:07', '2024-03-18 22:07:23', NULL, NULL, NULL);
INSERT INTO `role` VALUES (6, '三级CEO副', 1, '2024-03-05 16:01:20', '2024-03-05 16:01:21', NULL, NULL, NULL);
INSERT INTO `role` VALUES (7, '四级CEO正', 1, '2024-03-05 16:05:40', '2024-03-05 16:06:09', NULL, NULL, NULL);
INSERT INTO `role` VALUES (8, '四级CEO副', 1, '2024-03-05 16:05:42', '2024-03-05 16:06:10', NULL, NULL, NULL);
INSERT INTO `role` VALUES (9, '业务总监', 1, '2024-03-05 16:05:43', '2024-03-05 16:06:11', NULL, NULL, NULL);
INSERT INTO `role` VALUES (12, '方案解决人员', 1, '2024-03-05 16:05:48', '2024-03-05 16:06:14', NULL, NULL, NULL);
INSERT INTO `role` VALUES (13, '业务助理', 1, '2024-03-05 16:05:49', '2024-03-05 16:06:16', NULL, NULL, NULL);
INSERT INTO `role` VALUES (14, '商务助理', 1, '2024-03-05 16:05:51', '2024-03-05 16:06:17', NULL, NULL, NULL);
INSERT INTO `role` VALUES (15, '职员', 1, '2024-03-05 16:05:52', '2024-03-05 16:06:19', NULL, NULL, NULL);
INSERT INTO `role` VALUES (16, '信息支撑员', 1, '2024-03-05 16:05:53', '2024-03-05 16:06:20', NULL, NULL, NULL);
INSERT INTO `role` VALUES (17, '咨询服务员', 1, '2024-03-05 16:05:55', '2024-03-05 16:06:21', NULL, NULL, NULL);
INSERT INTO `role` VALUES (18, '项目执行员', 1, '2024-03-05 16:05:56', '2024-03-05 16:06:22', NULL, NULL, NULL);
INSERT INTO `role` VALUES (19, '投标业务员', 1, '2024-03-05 16:05:58', '2024-03-05 16:06:24', NULL, NULL, NULL);
INSERT INTO `role` VALUES (20, '项目员', 1, '2024-03-05 16:06:00', '2024-03-05 16:06:25', NULL, NULL, NULL);
INSERT INTO `role` VALUES (21, '信息料账员', 1, '2024-03-05 16:06:01', '2024-03-05 16:06:27', NULL, NULL, NULL);
INSERT INTO `role` VALUES (22, '信息质控员', 1, '2024-03-05 16:06:03', '2024-03-05 16:06:28', NULL, NULL, NULL);
INSERT INTO `role` VALUES (23, '库管员', 1, '2024-03-05 16:06:04', '2024-03-05 16:06:29', NULL, NULL, NULL);
INSERT INTO `role` VALUES (24, '调度运输员', 1, '2024-03-05 16:06:06', '2024-03-05 16:06:30', NULL, NULL, NULL);
INSERT INTO `role` VALUES (25, '调度员', 1, '2024-03-05 16:06:07', '2024-03-05 16:06:31', NULL, NULL, NULL);
INSERT INTO `role` VALUES (27, '公务员', 1, '2024-03-18 17:13:19', '2024-03-18 21:39:42', '', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
