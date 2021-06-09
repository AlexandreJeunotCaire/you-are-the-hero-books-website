CREATE SEQUENCE storyId;
CREATE SEQUENCE paragraphId;
CREATE SEQUENCE choiceId;

CREATE TABLE TalesUser(
  name VARCHAR(255) NOT NULL PRIMARY KEY,
  mail VARCHAR(255) NOT NULL,
  hash VARCHAR(255) NOT NULL
);

CREATE TABLE Paragraph(
  id INT NOT NULL PRIMARY KEY,
  author VARCHAR(255) NOT NULL CONSTRAINT fkParAuthor REFERENCES TalesUser(name),
  text VARCHAR(4000), -- Maximum value
  next INT CONSTRAINT fkNext REFERENCES Paragraph(id),
  ending INT NOT NULL
);

CREATE TABLE Story(
  id INT NOT NULL PRIMARY KEY,
  author VARCHAR(255) NOT NULL CONSTRAINT fkAuthor REFERENCES TalesUser(name),
  visibility VARCHAR(255) NOT NULL,
  published INT NOT NULL,
  firstPar INT NOT NULL CONSTRAINT fkFirstParagraph REFERENCES Paragraph(id),
  CONSTRAINT storyVisibility CHECK (visibility IN ('public', 'oninvite'))
);

CREATE TABLE Choice(
  id INT NOT NULL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  src INT NOT NULL CONSTRAINT fkParChoice REFERENCES Paragraph(id),
  dest INT CONSTRAINT fkChoiceDest REFERENCES Paragraph(id),
  cond INT CONSTRAINT fkChoiceCond REFERENCES Paragraph(id),
  editor VARCHAR(255) CONSTRAINT fkChoiceLock REFERENCES TalesUser(name)
);

CREATE TABLE ChoiceMap(
  reader VARCHAR(255) NOT NULL CONSTRAINT fkChoiceMapUser REFERENCES TalesUser(name),
  paragraph INT NOT NULL CONSTRAINT fkChoiceMapPar REFERENCES Paragraph(id),
  choice INT NOT NULL CONSTRAINT fkChoiceMapChoce REFERENCES Choice(id),
  PRIMARY KEY (reader, paragraph)
);

CREATE TABLE Invite(
  author VARCHAR(255) NOT NULL CONSTRAINT fkInviteAuthor REFERENCES TalesUser(name),
  invited VARCHAR(255) NOT NULL CONSTRAINT fkInviteUser REFERENCES TalesUser(name),
  story INT NOT NULL CONSTRAINT fkInviteStory REFERENCES Story(id),
  PRIMARY KEY (author, invited, story)
);

CREATE TABLE Authors(
  story INT NOT NULL CONSTRAINT fkAuthorStory REFERENCES Story(id),
  author VARCHAR(255) NOT NULL CONSTRAINT fkAuthorAuthor REFERENCES TalesUser(name),
  PRIMARY KEY (story, author)
);

CREATE TABLE Parent(
    parent INT NOT NULL CONSTRAINT fkParentParent REFERENCES Paragraph(id),
    child INT NOT NULL CONSTRAINT fkParentChild REFERENCES Paragraph(id),
    PRIMARY KEY (parent, child)
);

-- Password: 2./vyNXpNA774|g+
INSERT INTO TalesUser VALUES('willie', 'guillaume.ricard@grenoble-inp.org', '$2y$12$LZQhUFVUq7qsEKes.VxMaOH6zbukiTHcZkE8XwdYjr.a55vGxbGhu');

-- Password: Tanguy
INSERT INTO TALESUSER VALUES('Tanguy', 'tanguy.poinson@laposte.net', '$2y$12$LcTk0LB9bOaP9gv2Ztu0Gefhkz0EBcSvLKrKotqEAj6/tT20j6e2u');

-- Story from project document
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Votre tram arrive et ses portes s’ouvrent. Vous vous apprêtez à monter lorsque vous
entendez un bruit derrière le coin de l’immeuble voisin.', NULL, 0);

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Dans le tram, vous rencontrez…', NULL, 1);
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous montez dans le tram.', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'En tournant le coin, vous découvrez…', NULL, 1);
INSERT INTO Parent SELECT paragraphId.currval - 2, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous allez voir ce qui se passe.', paragraphId.currval - 2, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Story SELECT storyId.nextval, 'willie', 'public', 1, paragraphId.currval - 2 FROM dual;
INSERT INTO Authors VALUES (storyId.currval, 'willie');

-- Unfinished story
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Lorem ipsum dolor sit amet.
Ut varius commodo diam, ut gravida purus pulvinar vitae. In elementum venenatis lorem nec rhoncus. Donec eu urna sed neque pellentesque condimentum non vitae augue. Pellentesque ex felis, iaculis a convallis non, ultrices vel metus. Donec sed egestas risus. Fusce hendrerit tortor non dolor lobortis laoreet. In molestie maximus elit vitae mollis. Vestibulum in luctus nunc. Donec sed sodales libero. Pellentesque efficitur felis in ultricies placerat. Duis tincidunt augue quis enim malesuada, id eleifend erat aliquet. Morbi at porttitor nulla. Duis feugiat laoreet malesuada. Fusce luctus odio sed pulvinar mattis. Curabitur rutrum scelerisque suscipit. Aliquam justo ante, tincidunt id velit non, ullamcorper lacinia erat.

Praesent placerat laoreet ante, sed aliquam mi consectetur at. Aenean eu elit sem. Fusce id velit fringilla, tristique nisl vitae, imperdiet eros. Proin lacus augue, sodales sit amet lacus quis, efficitur convallis nisi. Morbi interdum porta molestie. Sed eget gravida quam. Duis varius pellentesque turpis at feugiat.

Donec a viverra erat, sit amet pulvinar sem. Morbi lobortis interdum nisi. Aliquam erat volutpat. Quisque tempor bibendum hendrerit. Quisque velit elit, varius non ante sed, pulvinar vulputate massa. Donec elementum massa leo, ac maximus massa commodo ornare. Fusce justo lacus, maximus vitae pellentesque in, malesuada id nulla. Integer vitae lacus pellentesque, imperdiet metus ac, dignissim ex. Duis placerat vel leo at interdum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.

Sed sodales quam eget elit aliquet, id porttitor tellus volutpat. Curabitur tempus ligula at erat feugiat, in hendrerit leo mollis. Ut quis efficitur urna. Donec blandit magna a magna maximus molestie. Pellentesque ex enim, suscipit sagittis vulputate in, pretium non metus. Ut cursus nibh in aliquet congue. Vestibulum nec rhoncus mi. Donec semper leo leo, ac tincidunt est posuere at. Nunc ac dui neque. Pellentesque gravida ac elit eu commodo. Etiam ligula felis, finibus at orci at, cursus molestie nisl.

Fusce sodales elementum magna, tempor aliquet nulla vehicula non. Proin tempor et dolor vel consequat. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Mauris et augue semper, laoreet magna at, mollis sem. Vivamus molestie euismod consequat. Nullam ipsum tortor, sagittis eu arcu eu, lobortis scelerisque urna. Morbi tellus nisi, accumsan convallis luctus eu, maximus a nisl. Sed ut faucibus eros.', NULL, 0);
INSERT INTO Story VALUES (storyId.nextval, 'willie', 'public', 0, paragraphId.currval);
INSERT INTO Authors VALUES (storyId.currval, 'willie');
INSERT INTO Choice VALUES (choiceId.nextval, 'Nouveau choix', paragraphId.currval, NULL, NULL, NULL);

-- Example story
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Ceci est une histoire de test.', NULL, 0);
INSERT INTO Story VALUES (storyId.nextval, 'willie', 'public', 1, paragraphId.currval);
INSERT INTO Authors VALUES (storyId.currval, 'willie');

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Ce paragraphe est le dernier et correspond à la fin de l''histoire', NULL, 1);

INSERT INTO Paragraph SELECT paragraphId.nextval, 'willie', 'Ce paragraphe correspond à une convergence de l''histoire et est stocké séparément.', paragraphId.currval - 1, 0 FROM dual;
INSERT INTO Parent SELECT paragraphId.currval, paragraphId.currval - 1 FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Ce paragraphe correspond au premier choix.', paragraphId.currval - 1, 0);
INSERT INTO Parent SELECT paragraphId.currval, paragraphId.currval - 1 FROM dual;
INSERT INTO Parent SELECT paragraphId.currval - 3, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Choix 1', paragraphId.currval - 3, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Ce paragraphe correspond au second choix.', paragraphId.currval - 2, 0);
INSERT INTO Parent SELECT paragraphId.currval - 4, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Choix 2', paragraphId.currval - 4, paragraphId.currval, NULL, NULL FROM dual;

-- Only choice string
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Voici une chaîne de paragraphes liés par des choix uniques.', NULL, 0);
INSERT INTO Story VALUES (storyId.nextval, 'willie', 'public', 1, paragraphId.currval);

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'tous sont affichés les uns à la suite des autres.', NULL, 0);
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Comme vous pouvez le constater,', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'c''est la fin', NULL, 1);
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Voilà,', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;

-- Conditional choice
INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Une clef se trouve sur le bureau.', NULL, 0);
INSERT INTO Story VALUES (storyId.nextval, 'willie', 'public', 1, paragraphId.currval);
INSERT INTO Authors VALUES (storyId.currval, 'willie');

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Muni de la clef vous descendez les escaliers.', NULL, 0);
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous ramassez la clef.', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Vous descendez les escaliers.', NULL, 0);
INSERT INTO Parent SELECT paragraphId.currval - 2, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous quittez la pièce.', paragraphId.currval - 2, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'Une porte verrouillée se dresse devant vous.', NULL, 0);
INSERT INTO Parent SELECT paragraphId.currval - 2, paragraphId.currval FROM dual;
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous arrivez en bas.', paragraphId.currval - 2, paragraphId.currval, NULL, NULL FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous arrivez en bas.', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'La porte ne bouge pas de ses gonds. Impossible de l''ouvrir sans la clef.', NULL, 1);
INSERT INTO Parent SELECT paragraphId.currval - 1, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous tentez de forcer la porte.', paragraphId.currval - 1, paragraphId.currval, NULL, NULL FROM dual;

INSERT INTO Paragraph VALUES (paragraphId.nextval, 'willie', 'De l''autre côté, un gâteau vous attendait (the cake is a lie).', NULL, 1);
INSERT INTO Parent SELECT paragraphId.currval - 2, paragraphId.currval FROM dual;
INSERT INTO Choice SELECT choiceId.nextval, 'Vous dévérouillez la porte avec la clef', paragraphId.currval - 2, paragraphId.currval, paragraphId.currval - 4, NULL FROM dual;
