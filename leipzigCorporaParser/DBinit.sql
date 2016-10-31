CREATE DATABASE `wordFreqDB`;

USE wordFreqDB;

CREATE TABLE `words` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `w_id` int(11) NOT NULL,
  `word` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `freq` int(10) unsigned DEFAULT NULL,
  `language` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `year` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `w_id` (`w_id`),
  CONSTRAINT `words_id_fk` FOREIGN KEY (`w_id`) REFERENCES `words` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8