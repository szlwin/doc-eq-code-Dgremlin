CREATE DATABASE `demo-test` DEFAULT CHARACTER SET utf8mb4;
use `demo-test`;
CREATE TABLE `order_info` (
  `o_id` int(11) NOT NULL AUTO_INCREMENT,
  `o_userId` int(11) DEFAULT NULL,
  `o_count` int(11) DEFAULT NULL,
  `o_date` datetime DEFAULT NULL,
  `o_totalPrice` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`o_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product_info` (
  `p_id` int(11) NOT NULL AUTO_INCREMENT,
  `p_orderId` int(11) DEFAULT NULL,
  `p_name` varchar(45) DEFAULT NULL,
  `p_count` int(11) DEFAULT NULL,
  `p_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`p_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_info` (
  `u_id` int(11) NOT NULL AUTO_INCREMENT,
  `u_name` varchar(45) DEFAULT NULL,
  `u_password` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE DATABASE `demo-test1` DEFAULT CHARACTER SET utf8mb4;
use `demo-test1`;
CREATE TABLE `order_info` (
  `o_id` int(11) NOT NULL AUTO_INCREMENT,
  `o_userId` int(11) DEFAULT NULL,
  `o_count` int(11) DEFAULT NULL,
  `o_date` datetime DEFAULT NULL,
  `o_totalPrice` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`o_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product_info` (
  `p_id` int(11) NOT NULL AUTO_INCREMENT,
  `p_orderId` int(11) DEFAULT NULL,
  `p_name` varchar(45) DEFAULT NULL,
  `p_count` int(11) DEFAULT NULL,
  `p_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`p_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_info` (
  `u_id` int(11) NOT NULL AUTO_INCREMENT,
  `u_name` varchar(45) DEFAULT NULL,
  `u_password` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
