-- quiz_question 테이블에 문항 순서 필드 추가
ALTER TABLE education.quiz_question
ADD COLUMN IF NOT EXISTS question_order INTEGER;

-- 기존 데이터에 순서 부여 (id 기준, UUID이므로 순서는 보장되지 않지만 기본값 부여)
UPDATE education.quiz_question q1
SET question_order = sub.row_num - 1
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY attempt_id ORDER BY id) AS row_num
    FROM education.quiz_question
    WHERE question_order IS NULL
) sub
WHERE q1.id = sub.id;

-- NOT NULL 제약조건 추가 (기본값 0)
ALTER TABLE education.quiz_question
ALTER COLUMN question_order SET DEFAULT 0;

-- 기존 NULL 값 처리
UPDATE education.quiz_question
SET question_order = 0
WHERE question_order IS NULL;

-- NOT NULL 제약조건 적용
ALTER TABLE education.quiz_question
ALTER COLUMN question_order SET NOT NULL;
