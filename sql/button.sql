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

 Date: 29/03/2024 01:20:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for button
-- ----------------------------
DROP TABLE IF EXISTS `button`;
CREATE TABLE `button`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '按钮权限字符',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_router` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父路由id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '按钮表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of button
-- ----------------------------
INSERT INTO `button` VALUES ('1-1-1', 'auth:role:list', '查询角色', '1-1');
INSERT INTO `button` VALUES ('1-1-2', 'auth:role:add', '新增角色', '1-1');
INSERT INTO `button` VALUES ('1-1-3', 'auth:role:update', '修改角色', '1-1');
INSERT INTO `button` VALUES ('1-1-4', 'auth:role:delete', '删除角色', '1-1');
INSERT INTO `button` VALUES ('1-2-1', 'auth:user:add', '新增用户', '1-2');
INSERT INTO `button` VALUES ('1-2-2', 'auth:user:list', '查询用户', '1-2');
INSERT INTO `button` VALUES ('1-2-3', 'auth:user:delete', '删除用户', '1-2');
INSERT INTO `button` VALUES ('1-2-4', 'auth:user:update', '修改用户', '1-2');
INSERT INTO `button` VALUES ('1-2-5', 'auth:user:export', '导出用户', '1-2');
INSERT INTO `button` VALUES ('1-2-6', 'auth:user:import', '导入用户', '1-2');
INSERT INTO `button` VALUES ('1-2-7', 'auth:user:assignRole', '分配角色', '1-2');
INSERT INTO `button` VALUES ('1-4-1', 'auth:dept:list', '查询部门', '1-4');
INSERT INTO `button` VALUES ('1-4-2', 'auth:dept:add', '新增部门', '1-4');
INSERT INTO `button` VALUES ('1-4-3', 'auth:dept:update', '修改部门', '1-4');
INSERT INTO `button` VALUES ('1-4-4', 'auth:dept:delete', '删除部门', '1-4');

SET FOREIGN_KEY_CHECKS = 1;
