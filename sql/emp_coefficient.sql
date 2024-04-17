-- MySQL dump 10.13  Distrib 8.0.35, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: performance-assessment
-- ------------------------------------------------------
-- Server version	8.0.35

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `emp_coefficient`
--

DROP TABLE IF EXISTS `emp_coefficient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emp_coefficient` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emp_id` bigint NOT NULL,
  `position_coefficient` decimal(5,2) NOT NULL,
  `region_coefficient_id` bigint NOT NULL,
  `wage` decimal(10,2) NOT NULL DEFAULT '4000.00',
  `performance_wage` decimal(10,2) NOT NULL DEFAULT '0.00',
  `state` smallint NOT NULL DEFAULT '1',
  `update_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  `create_user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `emp_id_UNIQUE` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='岗位系数&&地区系数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emp_coefficient`
--

LOCK TABLES `emp_coefficient` WRITE;
/*!40000 ALTER TABLE `emp_coefficient` DISABLE KEYS */;
INSERT INTO `emp_coefficient` VALUES (1,1,2.00,3,4000.00,1000.00,1,'2024-03-25 17:37:36','2024-03-24 00:44:05',1,NULL),(2,2,2.00,2,4000.00,1000.00,1,'2024-03-25 17:37:32','2024-03-24 00:44:05',1,NULL),(20,20,2.00,2,4000.00,2000.00,1,'2024-03-25 17:37:22','2024-03-24 00:44:05',1,NULL),(90,90,2.00,1,2000.00,2000.00,1,'2024-03-26 10:00:30','2024-03-24 00:44:05',1,NULL);
/*!40000 ALTER TABLE `emp_coefficient` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-03-28  9:28:48
