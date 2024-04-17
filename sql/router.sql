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

 Date: 29/03/2024 01:22:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for router
-- ----------------------------
DROP TABLE IF EXISTS `router`;
CREATE TABLE `router`  (
  `id` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` int NOT NULL,
  `type` int NOT NULL,
  `url` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `icon` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `children` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_UNIQUE`(`id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态路由表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of router
-- ----------------------------
INSERT INTO `router` VALUES ('1', '权限管理', 0, 1, '/adminAuth/getRoleList', NULL, NULL);
INSERT INTO `router` VALUES ('1-1', '角色列表', 1, 2, '/adminAuth/getRoleList', NULL, NULL);
INSERT INTO `router` VALUES ('1-2', '账号列表', 1, 2, '/adminAuth/adminList', NULL, NULL);
INSERT INTO `router` VALUES ('1-3', '账号详情', 1, 2, '/account/detail', NULL, NULL);
INSERT INTO `router` VALUES ('1-4', '权限列表', 1, 2, '/account/detail', NULL, NULL);
INSERT INTO `router` VALUES ('2', '审批', 0, 1, '/OA/fourthApprove', NULL, NULL);
INSERT INTO `router` VALUES ('2-1', '四级CEO审批', 2, 2, '/OA/fourthApprove', NULL, NULL);
INSERT INTO `router` VALUES ('2-2', '审批详情', 2, 2, '/OA/AuditDetial', NULL, NULL);
INSERT INTO `router` VALUES ('2-3', '已审批详情', 2, 2, '/OA/CompleteDetial', NULL, NULL);
INSERT INTO `router` VALUES ('2-4', '审批进度', 2, 2, '/OA/commissioner', NULL, NULL);
INSERT INTO `router` VALUES ('3', '申报', 0, 1, '/declare/fifthDeclare', NULL, NULL);
INSERT INTO `router` VALUES ('3-1', '员工申报', 3, 2, '/declare/fifthDeclare', NULL, NULL);
INSERT INTO `router` VALUES ('3-2', '四级单元CEO申报', 3, 2, '/declare/fourthDeclare', NULL, NULL);
INSERT INTO `router` VALUES ('3-3', '三级单元CEO申报', 3, 2, '/declare/thirdDeclare', NULL, NULL);
INSERT INTO `router` VALUES ('4', '绩效管理', 0, 1, '/performance/detail', NULL, NULL);
INSERT INTO `router` VALUES ('4-1', '岗位绩效', 4, 2, '/performance/detail', NULL, NULL);
INSERT INTO `router` VALUES ('4-2', '考核规则', 4, 2, '/performance/edit', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
