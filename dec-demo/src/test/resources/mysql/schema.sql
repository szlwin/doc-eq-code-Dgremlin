CREATE DATABASE IF NOT EXISTS `demo-test2` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
use `demo-test2`;
CREATE TABLE IF NOT EXISTS `order_detail_info` (
                                                   `o_d_id` int NOT NULL AUTO_INCREMENT,
                                                   `o_orderId` int DEFAULT NULL,
                                                   `o_userId` int DEFAULT NULL,
                                                   `o_productId` int DEFAULT NULL,
                                                   `o_count` int DEFAULT NULL,
                                                   `o_productName` varchar(20) DEFAULT NULL,
    `o_productPrice` decimal(10,2) DEFAULT NULL,
    `o_productAmount` decimal(10,2) DEFAULT NULL,
    `o_date` datetime DEFAULT NULL,
    PRIMARY KEY (`o_d_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `order_info` (
                                            `o_id` int NOT NULL AUTO_INCREMENT,
                                            `o_userId` int DEFAULT NULL,
                                            `o_count` int DEFAULT NULL,
                                            `o_totalPrice` decimal(10,2) DEFAULT NULL,
    `o_totalAmount` decimal(10,2) DEFAULT NULL,
    `o_status` int DEFAULT NULL,
    `o_date` datetime DEFAULT NULL,
    PRIMARY KEY (`o_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pay_detail_info` (
                                                 `o_id` int NOT NULL AUTO_INCREMENT,
                                                 `o_userId` int DEFAULT NULL,
                                                 `o_payId` int DEFAULT NULL,
                                                 `o_productId` int DEFAULT NULL,
                                                 `o_productName` varchar(20) DEFAULT NULL,
    `o_payPrice` decimal(10,2) DEFAULT NULL,
    `o_payAmount` decimal(10,2) DEFAULT NULL,
    `o_date` datetime DEFAULT NULL,
    PRIMARY KEY (`o_id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pay_info` (
                                          `o_id` int NOT NULL AUTO_INCREMENT,
                                          `o_userId` int DEFAULT NULL,
                                          `o_orderId` int DEFAULT NULL,
                                          `o_totalPrice` decimal(10,2) DEFAULT NULL,
    `o_totalAmount` decimal(10,2) DEFAULT NULL,
    `o_status` int DEFAULT NULL,
    `o_date` datetime DEFAULT NULL,
    PRIMARY KEY (`o_id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `product_info` (
                                              `p_id` int NOT NULL AUTO_INCREMENT,
                                              `p_orderId` int DEFAULT NULL,
                                              `p_name` varchar(45) DEFAULT NULL,
    `p_count` int DEFAULT NULL,
    `p_price` decimal(10,2) DEFAULT NULL,
    PRIMARY KEY (`p_id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_info` (
                                           `u_id` int NOT NULL AUTO_INCREMENT,
                                           `u_name` varchar(45) DEFAULT NULL,
    `u_status` int DEFAULT NULL,
    `u_activeTime` datetime DEFAULT NULL,
    `u_password` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`u_id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS `demo-test1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
INSERT INTO `user_info`
(
    `u_name`,
    `u_status`)
VALUES
    (
        'test',
        1);
INSERT INTO `demo-test2`.`user_info`
(
    `u_name`,
    `u_status`)
VALUES
    (
        'test',
        2);
use `demo-test1`;
CREATE TABLE IF NOT EXISTS `order_info` (
                                            `o_id` int NOT NULL AUTO_INCREMENT,
                                            `o_userId` int DEFAULT NULL,
                                            `o_count` int DEFAULT NULL,
                                            `o_date` datetime DEFAULT NULL,
                                            `o_totalPrice` decimal(10,2) DEFAULT NULL,
    PRIMARY KEY (`o_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `product_info` (
                                              `p_id` int NOT NULL AUTO_INCREMENT,
                                              `p_orderId` int DEFAULT NULL,
                                              `p_name` varchar(45) DEFAULT NULL,
    `p_count` int DEFAULT NULL,
    `p_price` decimal(10,2) DEFAULT NULL,
    PRIMARY KEY (`p_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_info` (
                                           `u_id` int NOT NULL AUTO_INCREMENT,
                                           `u_name` varchar(45) DEFAULT NULL,
    `u_password` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`u_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
