
--
-- Structure for table examplemuz_
--

DROP TABLE IF EXISTS examplemuz_;
CREATE TABLE examplemuz_ (
id_project int AUTO_INCREMENT,
name varchar(50) default '' NOT NULL,
description varchar(255) default '' NOT NULL,
image_url varchar(255) default '' NOT NULL,
cost int NOT NULL default 5,
PRIMARY KEY (id_project)
);
