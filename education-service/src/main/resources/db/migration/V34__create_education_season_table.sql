-- Create education_season table
CREATE TABLE IF NOT EXISTS education.education_season (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    education_id uuid NOT NULL,
    season_number int NOT NULL,
    start_at timestamptz NOT NULL,
    end_at timestamptz NOT NULL,
    is_active boolean DEFAULT false,
    created_at timestamptz DEFAULT NOW(),
    updated_at timestamptz DEFAULT NOW(),
    deleted_at timestamptz,
    CONSTRAINT fk_education_season_education FOREIGN KEY (education_id) REFERENCES education.education(id)
);

-- Add education_season_id to education_progress table
ALTER TABLE education.education_progress 
ADD COLUMN IF NOT EXISTS education_season_id uuid;

-- Add foreign key constraint for education_progress.education_season_id
ALTER TABLE education.education_progress
ADD CONSTRAINT fk_education_progress_education_season 
FOREIGN KEY (education_season_id) REFERENCES education.education_season(id);

-- Add education_season_id to education_video_progress table
ALTER TABLE education.education_video_progress 
ADD COLUMN IF NOT EXISTS education_season_id uuid;

-- Add foreign key constraint for education_video_progress.education_season_id
ALTER TABLE education.education_video_progress
ADD CONSTRAINT fk_education_video_progress_education_season 
FOREIGN KEY (education_season_id) REFERENCES education.education_season(id);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_education_season_education_id ON education.education_season(education_id);
CREATE INDEX IF NOT EXISTS idx_education_season_is_active ON education.education_season(is_active) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_education_progress_education_season_id ON education.education_progress(education_season_id);
CREATE INDEX IF NOT EXISTS idx_education_video_progress_education_season_id ON education.education_video_progress(education_season_id);

