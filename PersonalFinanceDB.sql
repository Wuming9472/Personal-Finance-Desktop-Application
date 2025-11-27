-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema personal_finance_db
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `personal_finance_db` ;

-- -----------------------------------------------------
-- Schema personal_finance_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `personal_finance_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `personal_finance_db` ;

-- -----------------------------------------------------
-- Table `personal_finance_db`.`categories`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`categories` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`categories` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT 'Nome della categoria (es. Alimentari, Stipendio)',
  PRIMARY KEY (`category_id`),
  UNIQUE INDEX `name` (`name` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 13
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_finance_db`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`users` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`users` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(16) NOT NULL,
  `password` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_finance_db`.`budgets`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`budgets` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`budgets` (
  `budget_id` INT NOT NULL AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `month` INT NOT NULL COMMENT 'Mese del budget (1-12)',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Importo massimo allocato',
  PRIMARY KEY (`budget_id`),
  UNIQUE INDEX `category_id` (`category_id` ASC, `month` ASC) VISIBLE,
  INDEX `fk_Budgets_Users1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `budgets_ibfk_1`
    FOREIGN KEY (`category_id`)
    REFERENCES `personal_finance_db`.`categories` (`category_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_Budgets_Users1`
    FOREIGN KEY (`user_id`)
    REFERENCES `personal_finance_db`.`users` (`user_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_finance_db`.`movements`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`movements` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`movements` (
  `movement_id` INT NOT NULL AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data della transazione',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Importo della transazione',
  `description` VARCHAR(255) NULL DEFAULT NULL COMMENT 'Breve descrizione o nota',
  `payment_method` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Metodo di pagamento (es. Carta di Credito, Contanti, Bonifico)',
  PRIMARY KEY (`movement_id`),
  INDEX `category_id` (`category_id` ASC) VISIBLE,
  INDEX `fk_Moviments_Users1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_Moviments_Users1`
    FOREIGN KEY (`user_id`)
    REFERENCES `personal_finance_db`.`users` (`user_id`),
  CONSTRAINT `movements_ibfk_1`
    FOREIGN KEY (`category_id`)
    REFERENCES `personal_finance_db`.`categories` (`category_id`)
    ON DELETE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
