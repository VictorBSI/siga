ALTER TABLE CORPORATIVO.DP_LOTACAO ADD ID_LOCALIDADE NUMBER(5,0);
ALTER TABLE CORPORATIVO.DP_LOTACAO ADD FOREIGN KEY (ID_LOCALIDADE) REFERENCES CORPORATIVO.CP_LOCALIDADE (ID_LOCALIDADE) ENABLE;