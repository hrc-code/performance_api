/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : performance

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 21/04/2024 01:42:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL,
  `position` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` smallint NOT NULL,
  `type_name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `kind` smallint NOT NULL,
  `kind_name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `audit_status` smallint NOT NULL DEFAULT 0 COMMENT '审核状态,0-未审核，1-正在审核，2-暂停，3-完成审核',
  `state` smallint NOT NULL DEFAULT 1,
  `INS` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_user` smallint NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `create_user` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_UNIQUE`(`id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1753769540742820099 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '0-未开始，1-进行中，2-已完成' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
