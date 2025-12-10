-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: personal_finance_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `budgets`
--

DROP TABLE IF EXISTS `budgets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `budgets` (
  `budget_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `user_id` int NOT NULL,
  `month` int NOT NULL COMMENT 'Mese del budget (1-12)',
  `year` int NOT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT 'Importo massimo allocato',
  PRIMARY KEY (`budget_id`),
  UNIQUE KEY `idx_user_cat_month_year` (`user_id`,`category_id`,`month`,`year`),
  KEY `fk_Budgets_Users1_idx` (`user_id`),
  KEY `budgets_ibfk_1` (`category_id`),
  CONSTRAINT `budgets_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_Budgets_Users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `budgets`
--

LOCK TABLES `budgets` WRITE;
/*!40000 ALTER TABLE `budgets` DISABLE KEYS */;
INSERT INTO `budgets` VALUES (1,1,3,11,2025,401.00),(2,2,3,11,2025,150.00),(3,3,3,11,2025,300.00),(4,4,3,11,2025,200.00),(5,5,3,11,2025,100.00),(6,6,3,11,2025,0.00),(7,7,3,11,2025,200.00),(8,8,3,11,2025,100.00),(9,1,3,12,2025,400.00),(10,2,3,12,2025,150.00),(11,3,3,12,2025,300.00),(12,4,3,12,2025,200.00),(13,5,3,12,2025,100.00),(14,6,3,12,2025,0.00),(15,7,3,12,2025,200.00),(16,8,3,12,2025,100.00),(21,1,10,12,2025,400.00),(22,2,10,12,2025,150.00),(23,3,10,12,2025,300.00),(24,4,10,12,2025,200.00),(25,5,10,12,2025,100.00),(26,6,10,12,2025,0.00),(27,7,10,12,2025,200.00),(28,8,10,12,2025,100.00),(29,1,12,12,2025,500.00),(30,2,12,12,2025,150.00),(31,3,12,12,2025,300.00),(32,4,12,12,2025,200.00),(33,5,12,12,2025,100.00),(34,6,12,12,2025,0.00),(35,7,12,12,2025,200.00),(36,8,12,12,2025,100.00);
/*!40000 ALTER TABLE `budgets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT 'Nome della categoria (es. Alimentari, Stipendio)',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Alimentari'),(8,'Altro'),(3,'Bollette'),(7,'Investimenti'),(5,'Salute'),(6,'Stipendio'),(4,'Svago'),(2,'Trasporti');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movements`
--

DROP TABLE IF EXISTS `movements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movements` (
  `movement_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `user_id` int NOT NULL,
  `type` varchar(20) NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data della transazione',
  `amount` decimal(10,2) NOT NULL COMMENT 'Importo della transazione',
  `title` varchar(100) DEFAULT NULL COMMENT 'Breve descrizione o nota',
  `payment_method` varchar(40) DEFAULT NULL COMMENT 'Metodo di pagamento (es. Carta di Credito, Contanti, Bonifico)',
  PRIMARY KEY (`movement_id`),
  KEY `category_id` (`category_id`),
  KEY `fk_Moviments_Users1_idx` (`user_id`),
  CONSTRAINT `fk_Moviments_Users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `movements_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=469 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movements`
--

LOCK TABLES `movements` WRITE;
/*!40000 ALTER TABLE `movements` DISABLE KEYS */;
INSERT INTO `movements` VALUES (188,1,3,'Uscita','2025-01-02 00:00:00',12.50,'Colazione bar','Carta'),(189,1,3,'Uscita','2025-01-03 00:00:00',45.00,'Spesa supermercato','Carta'),(190,1,3,'Entrata','2025-01-05 00:00:00',1200.00,'Stipendio','Bonifico'),(191,1,3,'Uscita','2025-01-06 00:00:00',9.90,'Abbonamento streaming','Carta'),(192,1,3,'Uscita','2025-01-08 00:00:00',17.20,'Pranzo fuori','Carta'),(193,1,3,'Uscita','2025-01-10 00:00:00',30.00,'Benzina','Carta'),(194,1,3,'Uscita','2025-01-12 00:00:00',8.50,'Caffè e snack','Contanti'),(195,1,3,'Uscita','2025-01-14 00:00:00',60.00,'Farmacia','Carta'),(196,1,3,'Entrata','2025-01-15 00:00:00',100.00,'Rimborso','Bonifico'),(197,1,3,'Uscita','2025-01-19 00:00:00',25.00,'Cena fuori','Carta'),(198,1,3,'Uscita','2025-01-22 00:00:00',42.80,'Spesa','Carta'),(199,1,3,'Uscita','2025-01-26 00:00:00',15.00,'Regalo','Contanti'),(200,1,3,'Uscita','2025-01-29 00:00:00',11.30,'Panetteria','Carta'),(201,1,3,'Entrata','2025-02-01 00:00:00',1200.00,'Stipendio','Bonifico'),(202,1,3,'Uscita','2025-02-02 00:00:00',38.00,'Spesa','Carta'),(203,1,3,'Uscita','2025-02-04 00:00:00',19.50,'Pranzo','Carta'),(204,1,3,'Uscita','2025-02-05 00:00:00',12.00,'Taxi','Contanti'),(205,1,3,'Uscita','2025-02-07 00:00:00',9.90,'Abbonamento app','Carta'),(206,1,3,'Uscita','2025-02-11 00:00:00',50.00,'Visita medica','Carta'),(207,1,3,'Uscita','2025-02-14 00:00:00',70.00,'Regalo San Valentino','Carta'),(208,1,3,'Uscita','2025-02-17 00:00:00',27.60,'Spesa','Carta'),(209,1,3,'Entrata','2025-02-20 00:00:00',80.00,'Vendita usato','Contanti'),(210,1,3,'Uscita','2025-02-24 00:00:00',16.40,'Cena veloce','Carta'),(211,1,3,'Uscita','2025-02-26 00:00:00',33.00,'Benzina','Carta'),(212,1,3,'Entrata','2025-03-01 00:00:00',1200.00,'Stipendio','Bonifico'),(213,1,3,'Uscita','2025-03-03 00:00:00',44.20,'Spesa','Carta'),(214,1,3,'Uscita','2025-03-05 00:00:00',10.00,'Caffetteria','Contanti'),(215,1,3,'Uscita','2025-03-08 00:00:00',90.00,'Telefono – rata','Carta'),(216,1,3,'Uscita','2025-03-10 00:00:00',20.00,'Regalo','Carta'),(217,1,3,'Uscita','2025-03-13 00:00:00',55.00,'Farmacia','Carta'),(218,1,3,'Entrata','2025-03-15 00:00:00',150.00,'Rimborso','Bonifico'),(219,1,3,'Uscita','2025-03-18 00:00:00',17.80,'Pranzo','Carta'),(220,1,3,'Uscita','2025-03-22 00:00:00',29.90,'Spesa','Carta'),(221,1,3,'Uscita','2025-03-27 00:00:00',14.00,'Panetteria','Contanti'),(222,1,3,'Uscita','2025-03-30 00:00:00',45.00,'Cena fuori','Carta'),(223,1,3,'Entrata','2025-04-01 00:00:00',1200.00,'Stipendio','Bonifico'),(224,1,3,'Uscita','2025-04-02 00:00:00',40.00,'Spesa','Carta'),(225,1,3,'Uscita','2025-04-04 00:00:00',18.00,'Pranzo','Carta'),(226,1,3,'Uscita','2025-04-07 00:00:00',12.90,'Abbonamento app','Carta'),(227,1,3,'Uscita','2025-04-10 00:00:00',30.00,'Benzina','Carta'),(228,1,3,'Uscita','2025-04-13 00:00:00',22.00,'Aperitivo','Carta'),(229,1,3,'Uscita','2025-04-16 00:00:00',65.00,'Farmacia','Carta'),(230,1,3,'Entrata','2025-04-17 00:00:00',90.00,'Rimborso','Bonifico'),(231,1,3,'Uscita','2025-04-20 00:00:00',26.40,'Spesa','Carta'),(232,1,3,'Uscita','2025-04-25 00:00:00',15.00,'Snack','Contanti'),(233,1,3,'Uscita','2025-04-29 00:00:00',50.00,'Cena fuori','Carta'),(234,1,3,'Entrata','2025-05-01 00:00:00',1200.00,'Stipendio','Bonifico'),(235,1,3,'Uscita','2025-05-02 00:00:00',39.00,'Spesa','Carta'),(236,1,3,'Uscita','2025-05-05 00:00:00',75.00,'Concerto','Carta'),(237,1,3,'Uscita','2025-05-07 00:00:00',17.50,'Pranzo','Carta'),(238,1,3,'Uscita','2025-05-09 00:00:00',13.00,'Abbonamento app','Carta'),(239,1,3,'Uscita','2025-05-11 00:00:00',31.00,'Benzina','Carta'),(240,1,3,'Entrata','2025-05-15 00:00:00',200.00,'Regalo compleanno','Contanti'),(241,1,3,'Uscita','2025-05-17 00:00:00',18.20,'Spesa','Carta'),(242,1,3,'Uscita','2025-05-21 00:00:00',25.00,'Cena','Carta'),(243,1,3,'Uscita','2025-05-27 00:00:00',60.00,'Abbigliamento','Carta'),(244,1,3,'Uscita','2025-05-30 00:00:00',7.50,'Caffè','Contanti'),(245,1,3,'Entrata','2025-06-01 00:00:00',1200.00,'Stipendio','Bonifico'),(246,1,3,'Uscita','2025-06-03 00:00:00',45.00,'Spesa','Carta'),(247,1,3,'Uscita','2025-06-05 00:00:00',12.50,'Pranzo','Carta'),(248,1,3,'Uscita','2025-06-07 00:00:00',30.00,'Benzina','Carta'),(249,1,3,'Uscita','2025-06-10 00:00:00',15.00,'Snack','Contanti'),(250,1,3,'Uscita','2025-06-13 00:00:00',80.00,'Regalo','Carta'),(251,1,3,'Entrata','2025-06-15 00:00:00',140.00,'Rimborso','Bonifico'),(252,1,3,'Uscita','2025-06-19 00:00:00',32.00,'Spesa','Carta'),(253,1,3,'Uscita','2025-06-23 00:00:00',24.50,'Cena','Carta'),(254,1,3,'Uscita','2025-06-27 00:00:00',10.00,'Panetteria','Contanti'),(255,1,3,'Entrata','2025-07-01 00:00:00',1200.00,'Stipendio','Bonifico'),(256,1,3,'Uscita','2025-07-02 00:00:00',48.00,'Spesa','Carta'),(257,1,3,'Uscita','2025-07-04 00:00:00',19.00,'Pranzo','Carta'),(258,1,3,'Uscita','2025-07-07 00:00:00',12.90,'Abbonamento app','Carta'),(259,1,3,'Uscita','2025-07-10 00:00:00',30.00,'Benzina','Carta'),(260,1,3,'Uscita','2025-07-14 00:00:00',22.50,'Aperitivo','Carta'),(261,1,3,'Uscita','2025-07-18 00:00:00',65.00,'Farmacia','Carta'),(262,1,3,'Uscita','2025-07-20 00:00:00',18.30,'Spesa','Carta'),(263,1,3,'Uscita','2025-07-25 00:00:00',55.00,'Cena fuori','Carta'),(264,1,3,'Uscita','2025-07-29 00:00:00',14.00,'Panetteria','Contanti'),(265,1,3,'Entrata','2025-08-01 00:00:00',1200.00,'Stipendio','Bonifico'),(266,1,3,'Uscita','2025-08-02 00:00:00',60.00,'Spesa','Carta'),(267,1,3,'Uscita','2025-08-05 00:00:00',22.00,'Pranzo','Carta'),(268,1,3,'Uscita','2025-08-07 00:00:00',14.90,'Abbonamento app','Carta'),(269,1,3,'Uscita','2025-08-10 00:00:00',35.00,'Benzina','Carta'),(270,1,3,'Uscita','2025-08-13 00:00:00',75.00,'Serata','Carta'),(271,1,3,'Entrata','2025-08-15 00:00:00',100.00,'Rimborso','Bonifico'),(272,1,3,'Uscita','2025-08-19 00:00:00',40.80,'Spesa','Carta'),(273,1,3,'Uscita','2025-08-24 00:00:00',18.00,'Gelato','Contanti'),(274,1,3,'Uscita','2025-08-29 00:00:00',55.00,'Cena fuori','Carta'),(275,1,3,'Entrata','2025-09-01 00:00:00',1200.00,'Stipendio','Bonifico'),(276,1,3,'Uscita','2025-09-02 00:00:00',42.00,'Spesa','Carta'),(277,1,3,'Uscita','2025-09-04 00:00:00',17.00,'Pranzo','Carta'),(278,1,3,'Uscita','2025-09-07 00:00:00',13.00,'Abbonamento app','Carta'),(279,1,3,'Uscita','2025-09-09 00:00:00',29.00,'Benzina','Carta'),(280,1,3,'Uscita','2025-09-13 00:00:00',20.00,'Aperitivo','Carta'),(281,1,3,'Uscita','2025-09-16 00:00:00',34.70,'Spesa','Carta'),(282,1,3,'Entrata','2025-09-17 00:00:00',85.00,'Vendita usato','Contanti'),(283,1,3,'Uscita','2025-09-21 00:00:00',22.00,'Cena','Carta'),(284,1,3,'Uscita','2025-09-28 00:00:00',11.30,'Panetteria','Contanti'),(285,1,3,'Entrata','2025-10-01 00:00:00',1200.00,'Stipendio','Bonifico'),(286,1,3,'Uscita','2025-10-03 00:00:00',49.00,'Spesa','Carta'),(287,1,3,'Uscita','2025-10-05 00:00:00',18.50,'Pranzo','Carta'),(288,1,3,'Uscita','2025-10-07 00:00:00',12.90,'Abbonamento app','Carta'),(289,1,3,'Uscita','2025-10-10 00:00:00',30.00,'Benzina','Carta'),(290,1,3,'Uscita','2025-10-14 00:00:00',55.00,'Farmacia','Carta'),(291,1,3,'Entrata','2025-10-16 00:00:00',100.00,'Rimborso','Bonifico'),(292,1,3,'Uscita','2025-10-19 00:00:00',22.40,'Spesa','Carta'),(293,1,3,'Uscita','2025-10-23 00:00:00',65.00,'Cena fuori','Carta'),(294,1,3,'Uscita','2025-10-28 00:00:00',8.50,'Snack','Contanti'),(295,1,3,'Entrata','2025-11-01 00:00:00',1200.00,'Stipendio','Bonifico'),(296,1,3,'Uscita','2025-11-02 00:00:00',47.00,'Spesa','Carta'),(297,1,3,'Uscita','2025-11-05 00:00:00',20.00,'Pranzo','Carta'),(298,1,3,'Uscita','2025-11-07 00:00:00',12.90,'Abbonamento app','Carta'),(299,1,3,'Uscita','2025-11-10 00:00:00',33.00,'Benzina','Carta'),(300,1,3,'Uscita','2025-11-14 00:00:00',18.00,'Aperitivo','Carta'),(301,1,3,'Entrata','2025-11-15 00:00:00',75.00,'Vendita usato','Contanti'),(302,1,3,'Uscita','2025-11-19 00:00:00',28.70,'Spesa','Carta'),(303,1,3,'Uscita','2025-11-23 00:00:00',24.00,'Cena','Carta'),(304,1,3,'Uscita','2025-11-28 00:00:00',10.00,'Panetteria','Contanti'),(324,1,3,'Uscita','2025-01-02 00:00:00',12.50,'Colazione bar','Carta'),(325,1,3,'Uscita','2025-01-03 00:00:00',45.00,'Spesa supermercato','Carta'),(326,1,3,'Entrata','2025-01-05 00:00:00',1200.00,'Stipendio','Bonifico'),(327,1,3,'Uscita','2025-01-06 00:00:00',9.90,'Abbonamento streaming','Carta'),(328,1,3,'Uscita','2025-01-08 00:00:00',17.20,'Pranzo fuori','Carta'),(329,1,3,'Uscita','2025-01-10 00:00:00',30.00,'Benzina','Carta'),(330,1,3,'Uscita','2025-01-12 00:00:00',8.50,'Caffè e snack','Contanti'),(331,1,3,'Uscita','2025-01-14 00:00:00',60.00,'Farmacia','Carta'),(332,1,3,'Entrata','2025-01-15 00:00:00',100.00,'Rimborso','Bonifico'),(333,1,3,'Uscita','2025-01-19 00:00:00',25.00,'Cena fuori','Carta'),(334,1,3,'Uscita','2025-01-22 00:00:00',42.80,'Spesa','Carta'),(335,1,3,'Uscita','2025-01-26 00:00:00',15.00,'Regalo','Contanti'),(336,1,3,'Uscita','2025-01-29 00:00:00',11.30,'Panetteria','Carta'),(337,1,3,'Entrata','2025-02-01 00:00:00',1200.00,'Stipendio','Bonifico'),(338,1,3,'Uscita','2025-02-02 00:00:00',38.00,'Spesa','Carta'),(339,1,3,'Uscita','2025-02-04 00:00:00',19.50,'Pranzo','Carta'),(340,1,3,'Uscita','2025-02-05 00:00:00',12.00,'Taxi','Contanti'),(341,1,3,'Uscita','2025-02-07 00:00:00',9.90,'Abbonamento app','Carta'),(342,1,3,'Uscita','2025-02-11 00:00:00',50.00,'Visita medica','Carta'),(343,1,3,'Uscita','2025-02-14 00:00:00',70.00,'Regalo San Valentino','Carta'),(344,1,3,'Uscita','2025-02-17 00:00:00',27.60,'Spesa','Carta'),(345,1,3,'Entrata','2025-02-20 00:00:00',80.00,'Vendita usato','Contanti'),(346,1,3,'Uscita','2025-02-24 00:00:00',16.40,'Cena veloce','Carta'),(347,1,3,'Uscita','2025-02-26 00:00:00',33.00,'Benzina','Carta'),(348,1,3,'Entrata','2025-03-01 00:00:00',1200.00,'Stipendio','Bonifico'),(349,1,3,'Uscita','2025-03-03 00:00:00',44.20,'Spesa','Carta'),(350,1,3,'Uscita','2025-03-05 00:00:00',10.00,'Caffetteria','Contanti'),(351,1,3,'Uscita','2025-03-08 00:00:00',90.00,'Telefono – rata','Carta'),(352,1,3,'Uscita','2025-03-10 00:00:00',20.00,'Regalo','Carta'),(353,1,3,'Uscita','2025-03-13 00:00:00',55.00,'Farmacia','Carta'),(354,1,3,'Entrata','2025-03-15 00:00:00',150.00,'Rimborso','Bonifico'),(355,1,3,'Uscita','2025-03-18 00:00:00',17.80,'Pranzo','Carta'),(356,1,3,'Uscita','2025-03-22 00:00:00',29.90,'Spesa','Carta'),(357,1,3,'Uscita','2025-03-27 00:00:00',14.00,'Panetteria','Contanti'),(358,1,3,'Uscita','2025-03-30 00:00:00',45.00,'Cena fuori','Carta'),(359,1,3,'Entrata','2025-04-01 00:00:00',1200.00,'Stipendio','Bonifico'),(360,1,3,'Uscita','2025-04-02 00:00:00',40.00,'Spesa','Carta'),(361,1,3,'Uscita','2025-04-04 00:00:00',18.00,'Pranzo','Carta'),(362,1,3,'Uscita','2025-04-07 00:00:00',12.90,'Abbonamento app','Carta'),(363,1,3,'Uscita','2025-04-10 00:00:00',30.00,'Benzina','Carta'),(364,1,3,'Uscita','2025-04-13 00:00:00',22.00,'Aperitivo','Carta'),(365,1,3,'Uscita','2025-04-16 00:00:00',65.00,'Farmacia','Carta'),(366,1,3,'Entrata','2025-04-17 00:00:00',90.00,'Rimborso','Bonifico'),(367,1,3,'Uscita','2025-04-20 00:00:00',26.40,'Spesa','Carta'),(368,1,3,'Uscita','2025-04-25 00:00:00',15.00,'Snack','Contanti'),(369,1,3,'Uscita','2025-04-29 00:00:00',50.00,'Cena fuori','Carta'),(370,1,3,'Entrata','2025-05-01 00:00:00',1200.00,'Stipendio','Bonifico'),(371,1,3,'Uscita','2025-05-02 00:00:00',39.00,'Spesa','Carta'),(372,1,3,'Uscita','2025-05-05 00:00:00',75.00,'Concerto','Carta'),(373,1,3,'Uscita','2025-05-07 00:00:00',17.50,'Pranzo','Carta'),(374,1,3,'Uscita','2025-05-09 00:00:00',13.00,'Abbonamento app','Carta'),(375,1,3,'Uscita','2025-05-11 00:00:00',31.00,'Benzina','Carta'),(376,1,3,'Entrata','2025-05-15 00:00:00',200.00,'Regalo compleanno','Contanti'),(377,1,3,'Uscita','2025-05-17 00:00:00',18.20,'Spesa','Carta'),(378,1,3,'Uscita','2025-05-21 00:00:00',25.00,'Cena','Carta'),(379,1,3,'Uscita','2025-05-27 00:00:00',60.00,'Abbigliamento','Carta'),(380,1,3,'Uscita','2025-05-30 00:00:00',7.50,'Caffè','Contanti'),(381,1,3,'Entrata','2025-06-01 00:00:00',1200.00,'Stipendio','Bonifico'),(382,1,3,'Uscita','2025-06-03 00:00:00',45.00,'Spesa','Carta'),(383,1,3,'Uscita','2025-06-05 00:00:00',12.50,'Pranzo','Carta'),(384,1,3,'Uscita','2025-06-07 00:00:00',30.00,'Benzina','Carta'),(385,1,3,'Uscita','2025-06-10 00:00:00',15.00,'Snack','Contanti'),(386,1,3,'Uscita','2025-06-13 00:00:00',80.00,'Regalo','Carta'),(387,1,3,'Entrata','2025-06-15 00:00:00',140.00,'Rimborso','Bonifico'),(388,1,3,'Uscita','2025-06-19 00:00:00',32.00,'Spesa','Carta'),(389,1,3,'Uscita','2025-06-23 00:00:00',24.50,'Cena','Carta'),(390,1,3,'Uscita','2025-06-27 00:00:00',10.00,'Panetteria','Contanti'),(391,1,3,'Entrata','2025-07-01 00:00:00',1200.00,'Stipendio','Bonifico'),(392,1,3,'Uscita','2025-07-02 00:00:00',48.00,'Spesa','Carta'),(393,1,3,'Uscita','2025-07-04 00:00:00',19.00,'Pranzo','Carta'),(394,1,3,'Uscita','2025-07-07 00:00:00',12.90,'Abbonamento app','Carta'),(395,1,3,'Uscita','2025-07-10 00:00:00',30.00,'Benzina','Carta'),(396,1,3,'Uscita','2025-07-14 00:00:00',22.50,'Aperitivo','Carta'),(397,1,3,'Uscita','2025-07-18 00:00:00',65.00,'Farmacia','Carta'),(398,1,3,'Uscita','2025-07-20 00:00:00',18.30,'Spesa','Carta'),(399,1,3,'Uscita','2025-07-25 00:00:00',55.00,'Cena fuori','Carta'),(400,1,3,'Uscita','2025-07-29 00:00:00',14.00,'Panetteria','Contanti'),(401,1,3,'Entrata','2025-08-01 00:00:00',1200.00,'Stipendio','Bonifico'),(402,1,3,'Uscita','2025-08-02 00:00:00',60.00,'Spesa','Carta'),(403,1,3,'Uscita','2025-08-05 00:00:00',22.00,'Pranzo','Carta'),(404,1,3,'Uscita','2025-08-07 00:00:00',14.90,'Abbonamento app','Carta'),(405,1,3,'Uscita','2025-08-10 00:00:00',35.00,'Benzina','Carta'),(406,1,3,'Uscita','2025-08-13 00:00:00',75.00,'Serata','Carta'),(407,1,3,'Entrata','2025-08-15 00:00:00',100.00,'Rimborso','Bonifico'),(408,1,3,'Uscita','2025-08-19 00:00:00',40.80,'Spesa','Carta'),(409,1,3,'Uscita','2025-08-24 00:00:00',18.00,'Gelato','Contanti'),(410,1,3,'Uscita','2025-08-29 00:00:00',55.00,'Cena fuori','Carta'),(411,1,3,'Entrata','2025-09-01 00:00:00',1200.00,'Stipendio','Bonifico'),(412,1,3,'Uscita','2025-09-02 00:00:00',42.00,'Spesa','Carta'),(413,1,3,'Uscita','2025-09-04 00:00:00',17.00,'Pranzo','Carta'),(414,1,3,'Uscita','2025-09-07 00:00:00',13.00,'Abbonamento app','Carta'),(415,1,3,'Uscita','2025-09-09 00:00:00',29.00,'Benzina','Carta'),(416,1,3,'Uscita','2025-09-13 00:00:00',20.00,'Aperitivo','Carta'),(417,1,3,'Uscita','2025-09-16 00:00:00',34.70,'Spesa','Carta'),(418,1,3,'Entrata','2025-09-17 00:00:00',85.00,'Vendita usato','Contanti'),(419,1,3,'Uscita','2025-09-21 00:00:00',22.00,'Cena','Carta'),(420,1,3,'Uscita','2025-09-28 00:00:00',11.30,'Panetteria','Contanti'),(421,1,3,'Entrata','2025-10-01 00:00:00',1200.00,'Stipendio','Bonifico'),(422,1,3,'Uscita','2025-10-03 00:00:00',49.00,'Spesa','Carta'),(423,1,3,'Uscita','2025-10-05 00:00:00',18.50,'Pranzo','Carta'),(424,1,3,'Uscita','2025-10-07 00:00:00',12.90,'Abbonamento app','Carta'),(425,1,3,'Uscita','2025-10-10 00:00:00',30.00,'Benzina','Carta'),(426,1,3,'Uscita','2025-10-14 00:00:00',55.00,'Farmacia','Carta'),(427,1,3,'Entrata','2025-10-16 00:00:00',100.00,'Rimborso','Bonifico'),(428,1,3,'Uscita','2025-10-19 00:00:00',22.40,'Spesa','Carta'),(429,1,3,'Uscita','2025-10-23 00:00:00',65.00,'Cena fuori','Carta'),(430,1,3,'Uscita','2025-10-28 00:00:00',8.50,'Snack','Contanti'),(431,1,3,'Entrata','2025-11-01 00:00:00',1200.00,'Stipendio','Bonifico'),(432,1,3,'Uscita','2025-11-02 00:00:00',47.00,'Spesa','Carta'),(433,1,3,'Uscita','2025-11-05 00:00:00',20.00,'Pranzo','Carta'),(434,1,3,'Uscita','2025-11-07 00:00:00',12.90,'Abbonamento app','Carta'),(435,1,3,'Uscita','2025-11-10 00:00:00',33.00,'Benzina','Carta'),(436,1,3,'Uscita','2025-11-14 00:00:00',18.00,'Aperitivo','Carta'),(437,1,3,'Entrata','2025-11-15 00:00:00',75.00,'Vendita usato','Contanti'),(438,1,3,'Uscita','2025-11-19 00:00:00',28.70,'Spesa','Carta'),(439,1,3,'Uscita','2025-11-23 00:00:00',24.00,'Cena','Carta'),(440,1,3,'Uscita','2025-11-28 00:00:00',10.00,'Panetteria','Contanti'),(441,1,3,'Entrata','2025-12-01 00:00:00',1200.00,'Stipendio','Bonifico'),(442,1,3,'Uscita','2025-12-02 00:00:00',52.00,'Spesa','Carta'),(443,1,3,'Uscita','2025-12-04 00:00:00',21.00,'Pranzo','Carta'),(444,1,3,'Uscita','2025-12-06 00:00:00',12.90,'Abbonamento app','Carta'),(445,1,3,'Uscita','2025-12-09 00:00:00',32.00,'Benzina','Carta'),(446,1,3,'Uscita','2025-12-12 00:00:00',70.00,'Regali di Natale','Carta'),(447,1,3,'Entrata','2025-12-15 00:00:00',150.00,'Bonus','Bonifico'),(448,1,3,'Uscita','2025-12-18 00:00:00',30.50,'Spesa','Carta'),(449,1,3,'Uscita','2025-12-22 00:00:00',65.00,'Cena fuori','Carta'),(450,1,3,'Uscita','2025-12-27 00:00:00',13.00,'Dolci','Contanti'),(451,1,3,'Entrata','2025-11-10 00:00:00',500.00,'',''),(455,1,3,'Uscita','2025-12-03 00:00:00',4.00,'',''),(460,3,3,'Uscita','2025-12-10 00:00:00',50.00,'',''),(465,1,3,'Uscita','2025-12-10 00:00:00',5000.00,'',''),(466,5,3,'Uscita','2025-12-10 00:00:00',1000.00,'',''),(467,4,3,'Uscita','2025-12-11 00:00:00',20000.00,'sono un coglione che spende tutto lo stipendio in donnacce e cripto','Carta di credito'),(468,1,12,'Uscita','2025-12-10 00:00:00',600.00,'','');
/*!40000 ALTER TABLE `movements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_questions`
--

DROP TABLE IF EXISTS `security_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `security_questions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `question` varchar(255) NOT NULL,
  `answer` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `security_questions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `security_questions`
--

LOCK TABLES `security_questions` WRITE;
/*!40000 ALTER TABLE `security_questions` DISABLE KEYS */;
INSERT INTO `security_questions` VALUES (4,12,'Nome del tuo primo animale?','a'),(5,12,'Città in cui sei nato/a?','a'),(6,12,'Colore preferito?','a'),(7,8,'Nome del tuo primo animale?','a'),(8,8,'Città in cui sei nato/a?','a'),(9,8,'Colore preferito?','a'),(10,10,'Nome del tuo primo animale?','Pelato'),(11,10,'Colore preferito?','Negro'),(12,10,'Titolo del tuo film preferito?','Miglioverde'),(13,12,'Nome del tuo primo animale?','a'),(14,12,'Città in cui sei nato/a?','a'),(15,12,'Colore preferito?','a');
/*!40000 ALTER TABLE `security_questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(16) NOT NULL,
  `password` varchar(32) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Filippo','Napoli2025'),(2,'Ingrid','IphoneMerda'),(3,'aaaa','aaaa'),(8,'bbbb','bbbb'),(9,'paperino32','diodato'),(10,'Leonardo','Cinelli'),(12,'cccc','bbbb');
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

-- Dump completed on 2025-12-10 12:13:01
