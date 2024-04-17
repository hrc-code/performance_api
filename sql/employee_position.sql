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

 Date: 28/03/2024 23:58:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for employee_position
-- ----------------------------
DROP TABLE IF EXISTS `employee_position`;
CREATE TABLE `employee_position`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emp_id` bigint NOT NULL,
  `position_id` bigint NOT NULL,
  `posi_percent` decimal(10, 2) NOT NULL DEFAULT 100.00,
  `state` smallint NOT NULL DEFAULT 1,
  `ins` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_user` bigint NULL DEFAULT NULL,
  `update_user` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_UNIQUE`(`id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1753748227122823234 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of employee_position
-- ----------------------------
INSERT INTO `employee_position` VALUES (1753748227122823231, 1, 1753769540742819866, 100.00, 1, NULL, '2024-03-25 18:26:16', '2024-03-25 18:26:16', 1, 1);
INSERT INTO `employee_position` VALUES (1753748227122823232, 6, 1753769540742819865, 100.00, 1, NULL, '2024-03-25 18:26:21', '2024-03-25 18:26:21', 1, 1);
INSERT INTO `employee_position` VALUES (1753748227122823233, 7, 1753769540742819866, 100.00, 1, NULL, '2024-03-25 18:26:26', '2024-03-25 18:26:26', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
