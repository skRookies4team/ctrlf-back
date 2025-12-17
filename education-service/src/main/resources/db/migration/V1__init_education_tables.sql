-- Ensure schema exists
CREATE SCHEMA IF NOT EXISTS education;

-- quiz_attempt
CREATE TABLE IF NOT EXISTS education.quiz_attempt (
    id UUID PRIMARY KEY,
    user_uuid UUID,
    education_id UUID,
    score INTEGER,
    passed BOOLEAN,
    attempt_no INTEGER,
    created_at TIMESTAMP,
    time_limit INTEGER,
    submitted_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- quiz_question
CREATE TABLE IF NOT EXISTS education.quiz_question (
    id UUID PRIMARY KEY,
    attempt_id UUID,
    question TEXT,
    options TEXT,
    correct_option_idx INTEGER,
    explanation TEXT,
    user_selected_option_idx INTEGER,
    deleted_at TIMESTAMP
);

-- quiz_leave_tracking
CREATE TABLE IF NOT EXISTS education.quiz_leave_tracking (
    id UUID PRIMARY KEY,
    attempt_id UUID,
    leave_count INTEGER,
    total_leave_seconds INTEGER,
    last_leave_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- education_video_progress
CREATE TABLE IF NOT EXISTS education.education_video_progress (
    id UUID PRIMARY KEY,
    user_uuid UUID,
    education_id UUID,
    video_id UUID,
    progress INTEGER,
    last_position_seconds INTEGER,
    total_watch_seconds INTEGER,
    is_completed BOOLEAN,
    updated_at TIMESTAMP,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- education_script
CREATE TABLE IF NOT EXISTS education.education_script (
    id UUID PRIMARY KEY,
    education_id UUID,
    source_doc_id UUID,
    title VARCHAR(255),
    total_duration_sec INTEGER,
    version INTEGER,
    llm_model VARCHAR(255),
    generation_prompt_hash VARCHAR(64),
    raw_payload JSONB,
    created_by UUID,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- education_script_chapter
CREATE TABLE IF NOT EXISTS education.education_script_chapter (
    id UUID PRIMARY KEY,
    script_id UUID,
    chapter_index INTEGER,
    title VARCHAR(255),
    duration_sec INTEGER,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- education_script_scene
CREATE TABLE IF NOT EXISTS education.education_script_scene (
    id UUID PRIMARY KEY,
    script_id UUID,
    chapter_id UUID,
    scene_index INTEGER,
    purpose VARCHAR(255),
    narration TEXT,
    caption TEXT,
    visual TEXT,
    duration_sec INTEGER,
    source_chunk_indexes INT[],
    confidence_score FLOAT,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP
);


