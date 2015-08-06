# --- !Ups

CREATE TABLE rankings (
  id        SERIAL PRIMARY KEY,
  div       VARCHAR(10),
  pos       INTEGER,
  team      VARCHAR(255),
  matches   INTEGER,
  wins      INTEGER,
  losses    INTEGER,
  draws     INTEGER,
  goalsPlus INTEGER,
  goalsMin  INTEGER,
  points    INTEGER,
  per       INTEGER
);

CREATE TABLE matches (
  id            SERIAL PRIMARY KEY,
  div           VARCHAR(10),
  dateTime      TIMESTAMP,
  home          VARCHAR(255),
  away          VARCHAR(255),
  rh            INTEGER,
  ra            INTEGER,
  status        VARCHAR(255),
  md            INTEGER,
  regnumberhome INTEGER,
  regnumberaway INTEGER
);

  # --- !Downs

DROP TABLE matches;
DROP TABLE rankings;