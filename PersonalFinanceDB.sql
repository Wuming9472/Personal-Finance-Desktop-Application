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
-- Table `personal_finance_db`.`Categories`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`Categories` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`Categories` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT 'Nome della categoria (es. Alimentari, Stipendio)',
  PRIMARY KEY (`category_id`),
  UNIQUE INDEX `name` (`name` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 13
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_finance_db`.`Budgets`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`Budgets` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`Budgets` (
  `budget_id` INT NOT NULL AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `month` INT NOT NULL COMMENT 'Mese del budget (1-12)',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Importo massimo allocato',
  PRIMARY KEY (`budget_id`),
  UNIQUE INDEX `category_id` (`category_id` ASC, `month` ASC) VISIBLE,
  CONSTRAINT `budgets_ibfk_1`
    FOREIGN KEY (`category_id`)
    REFERENCES `personal_finance_db`.`Categories` (`category_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_finance_db`.`Moviments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `personal_finance_db`.`Moviments` ;

CREATE TABLE IF NOT EXISTS `personal_finance_db`.`Moviments` (
  `movement_id` INT NOT NULL AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data della transazione',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Importo della transazione',
  `description` VARCHAR(255) NULL COMMENT 'Breve descrizione o nota',
  `payment_method` VARCHAR(50) NULL COMMENT 'Metodo di pagamento (es. Carta di Credito, Contanti, Bonifico)',
  PRIMARY KEY (`movement_id`),
  INDEX `category_id` (`category_id` ASC) VISIBLE,
  CONSTRAINT `movements_ibfk_1`
    FOREIGN KEY (`category_id`)
    REFERENCES `personal_finance_db`.`Categories` (`category_id`)
    ON DELETE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
