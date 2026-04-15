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
-- Current Database: `erp_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `erp_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `erp_db`;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `credits` int NOT NULL,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'CS101','Intro to Programming',3),(2,'CS240','Data Structures',4),(3,'MTH210','Discrete Structures',4),(4,'MTH201','RA',4),(6,'CSE301','DBMS',4);
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `enrollment_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `section_id` int NOT NULL,
  `status` varchar(20) DEFAULT 'ENROLLED',
  `final_grade` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `student_id` (`student_id`,`section_id`),
  KEY `section_id` (`section_id`),
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
INSERT INTO `enrollments` VALUES (67,3,6,'ENROLLED','A'),(68,3,8,'ENROLLED',NULL),(69,7,6,'ENROLLED','A');
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `grade_id` int NOT NULL AUTO_INCREMENT,
  `enrollment_id` int NOT NULL,
  `component` varchar(50) NOT NULL,
  `score` double DEFAULT NULL,
  `weightage` int DEFAULT '0',
  PRIMARY KEY (`grade_id`),
  KEY `enrollment_id` (`enrollment_id`),
  CONSTRAINT `grades_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`enrollment_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (144,67,'End term',90,100),(145,69,'End term',95,100);
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructors`
--

DROP TABLE IF EXISTS `instructors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructors` (
  `instructor_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`instructor_id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructors`
--

LOCK TABLES `instructors` WRITE;
/*!40000 ALTER TABLE `instructors` DISABLE KEYS */;
INSERT INTO `instructors` VALUES (1,2,'Prof. Alan Turing','alan@univ.edu','Computer Science'),(2,6,'Gaurav Ghansela','ganselagoat@mail.com','Computer Science');
/*!40000 ALTER TABLE `instructors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `message` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'You were assigned a new section: New section created (Section 3) for Course ID 3','2025-11-24 12:31:02',1),(2,3,'New section opened: New section created (Section 3) for Course ID 3','2025-11-24 12:31:02',1),(3,4,'New section opened: New section created (Section 3) for Course ID 3','2025-11-24 12:31:02',0),(4,3,'Grades released for section 2. Check your Academic Record.','2025-11-24 13:11:23',1),(5,3,'Grades released for section 2. Check your Academic Record.','2025-11-24 13:17:27',1),(6,3,'Grades released for section 2. Check your Academic Record.','2025-11-24 13:34:33',1),(7,6,'You were assigned a new section: New section created (Section 4) for Course ID 4','2025-11-24 13:38:25',0),(8,3,'New section opened: New section created (Section 4) for Course ID 4','2025-11-24 13:38:25',1),(9,4,'New section opened: New section created (Section 4) for Course ID 4','2025-11-24 13:38:25',0),(10,5,'New section opened: New section created (Section 4) for Course ID 4','2025-11-24 13:38:25',0),(11,3,'Grades released for section 3. Check your Academic Record.','2025-11-27 14:26:19',0),(12,7,'Grades released for section 3. Check your Academic Record.','2025-11-27 14:26:19',1),(13,2,'You were assigned a new section: New section created (Section 5) for Course ID 6','2025-11-27 14:29:46',0),(14,3,'New section opened: New section created (Section 5) for Course ID 6','2025-11-27 14:29:46',0),(15,5,'New section opened: New section created (Section 5) for Course ID 6','2025-11-27 14:29:46',0),(16,7,'New section opened: New section created (Section 5) for Course ID 6','2025-11-27 14:29:46',1),(17,8,'New section opened: New section created (Section 5) for Course ID 6','2025-11-27 14:29:46',0),(18,2,'You were assigned a new section: New section created (Section 6) for Course ID 1','2025-11-27 16:46:36',0),(19,3,'New section opened: New section created (Section 6) for Course ID 1','2025-11-27 16:46:36',0),(20,5,'New section opened: New section created (Section 6) for Course ID 1','2025-11-27 16:46:36',0),(21,7,'New section opened: New section created (Section 6) for Course ID 1','2025-11-27 16:46:36',1),(22,8,'New section opened: New section created (Section 6) for Course ID 1','2025-11-27 16:46:36',0),(23,6,'You were assigned a new section: New section created (Section 7) for Course ID 1','2025-11-27 16:46:56',0),(24,3,'New section opened: New section created (Section 7) for Course ID 1','2025-11-27 16:46:56',0),(25,5,'New section opened: New section created (Section 7) for Course ID 1','2025-11-27 16:46:56',0),(26,7,'New section opened: New section created (Section 7) for Course ID 1','2025-11-27 16:46:56',1),(27,8,'New section opened: New section created (Section 7) for Course ID 1','2025-11-27 16:46:56',0),(28,2,'You were assigned a new section: New section created (Section 8) for Course ID 2','2025-11-27 16:47:32',0),(29,3,'New section opened: New section created (Section 8) for Course ID 2','2025-11-27 16:47:32',0),(30,5,'New section opened: New section created (Section 8) for Course ID 2','2025-11-27 16:47:32',0),(31,7,'New section opened: New section created (Section 8) for Course ID 2','2025-11-27 16:47:32',1),(32,8,'New section opened: New section created (Section 8) for Course ID 2','2025-11-27 16:47:32',0);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `section_weightage`
--

DROP TABLE IF EXISTS `section_weightage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section_weightage` (
  `section_id` int NOT NULL,
  `component` varchar(50) NOT NULL,
  `weightage` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`section_id`,`component`),
  CONSTRAINT `section_weightage_ibfk_1` FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section_weightage`
--

LOCK TABLES `section_weightage` WRITE;
/*!40000 ALTER TABLE `section_weightage` DISABLE KEYS */;
INSERT INTO `section_weightage` VALUES (6,'End term',100);
/*!40000 ALTER TABLE `section_weightage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sections`
--

DROP TABLE IF EXISTS `sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sections` (
  `section_id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NOT NULL,
  `instructor_id` int DEFAULT NULL,
  `day_time` varchar(50) DEFAULT NULL,
  `room` varchar(50) DEFAULT NULL,
  `capacity` int NOT NULL,
  `semester` varchar(20) DEFAULT NULL,
  `year` int DEFAULT NULL,
  `drop_deadline` date DEFAULT NULL,
  `grades_released` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`section_id`),
  KEY `course_id` (`course_id`),
  KEY `instructor_id` (`instructor_id`),
  CONSTRAINT `sections_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `sections_ibfk_2` FOREIGN KEY (`instructor_id`) REFERENCES `instructors` (`instructor_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sections`
--

LOCK TABLES `sections` WRITE;
/*!40000 ALTER TABLE `sections` DISABLE KEYS */;
INSERT INTO `sections` VALUES (6,1,1,'MW 10:00-11:30','C01',50,'Winter',2025,NULL,0),(7,1,2,'MW 10:00-11:30','C02',50,'Winter',2025,NULL,0),(8,2,1,'TF 13:00-14:30','C101',1,'Winter',2025,NULL,0);
/*!40000 ALTER TABLE `sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settings` (
  `setting_key` varchar(50) NOT NULL,
  `setting_value` varchar(255) NOT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES ('course_registration_deadline','2025-11-30'),('maintenance_mode','false');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `user_id` int NOT NULL,
  `roll_no` varchar(20) NOT NULL,
  `program` varchar(100) DEFAULT NULL,
  `year` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `roll_no` (`roll_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (2,'2024085','Btech',2024),(3,'S1001','B.Tech CSE',2),(5,'2024275','B.Tech',2024),(7,'2024468','Btech',2024),(8,'2024099','Btech',2024);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `auth_db`;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `pass_hash` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$12$.2jngtzx1Spvqxi7jPEcxe30JxDq3KKT9UBaVjqsUpmyhmkrjaHlO','ADMIN','ACTIVE'),(2,'inst1','$2a$12$8SIgZ8iSvuJ/1nAO3leNveeSRVzhBu6euFcrHFGABdJ03FTJNVyKC','INSTRUCTOR','ACTIVE'),(3,'stu1','$2a$12$xIjoYi2AtKaFIhoEshkrPe7xyJc8hb7t3EePhM5In1/3n0rV30QsO','STUDENT','ACTIVE'),(5,'stu3','$2a$12$6p4HBbTMk7N4mVaT/nUubutaIhJLBS3N9NqS2fima8.nCZrnxOavq','STUDENT','ACTIVE'),(6,'inst2','$2a$12$I/ObMU0UKVqWPohO5Vspie51S9AgnsSCRvfW2B72ulOGK57KMeYZC','INSTRUCTOR','ACTIVE'),(7,'stu2','$2a$12$Z8wHJyNVysYwS/yhc4opmeaJPtMX9FMK/xUjgdZjRe/so7AVMawqW','STUDENT','ACTIVE'),(8,'stu4','$2a$12$nZHzb5bt9Glef5MnUHdsEONXt8AbFnvfWG13Pj3HCAo0yh3cv6w4C','STUDENT','ACTIVE');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 22:32:46
