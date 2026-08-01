INSERT IGNORE INTO sport (id, name) VALUES
(1, '헬스'),
(2, '러닝'),
(3, '축구'),
(4, '농구'),
(5, '배드민턴'),
(6, '테니스'),
(7, '클라이밍'),
(8, '수영'),
(9, '탁구');

UPDATE sport SET image_url = '/images/sports/fitness.png' WHERE id = 1;
UPDATE sport SET image_url = '/images/sports/running.png' WHERE id = 2;
UPDATE sport SET image_url = '/images/sports/soccer.png' WHERE id = 3;
UPDATE sport SET image_url = '/images/sports/basketball.png' WHERE id = 4;
UPDATE sport SET image_url = '/images/sports/badminton.png' WHERE id = 5;
UPDATE sport SET image_url = '/images/sports/tennis.png' WHERE id = 6;
UPDATE sport SET image_url = '/images/sports/climbing.png' WHERE id = 7;
UPDATE sport SET image_url = '/images/sports/swimming.png' WHERE id = 8;
UPDATE sport SET image_url = '/images/sports/table-tennis.png' WHERE id = 9;

INSERT IGNORE INTO region (id, name) VALUES
(1, '강남구'),
(2, '강동구'),
(3, '강북구'),
(4, '강서구'),
(5, '관악구'),
(6, '광진구'),
(7, '구로구'),
(8, '금천구'),
(9, '노원구'),
(10, '도봉구'),
(11, '동대문구'),
(12, '동작구'),
(13, '마포구'),
(14, '서대문구'),
(15, '서초구'),
(16, '성동구'),
(17, '성북구'),
(18, '송파구'),
(19, '양천구'),
(20, '영등포구'),
(21, '용산구'),
(22, '은평구'),
(23, '종로구'),
(24, '중구'),
(25, '중랑구');
