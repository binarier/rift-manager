
/* Drop Tables */

DROP TABLE PROXY_HOST;




/* Create Tables */

CREATE TABLE PROXY_HOST
(
	USERNAME varchar(40) NOT NULL,
	HOSTNAME varchar(200) NOT NULL,
	PRIMARY KEY (USERNAME, HOSTNAME)
);



