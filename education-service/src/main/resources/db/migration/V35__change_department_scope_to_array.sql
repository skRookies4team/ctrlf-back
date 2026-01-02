-- Change department_scope from text to text[] array
-- If column doesn't exist, create it as array type
-- If column exists as text, convert JSON string data to array format

-- Step 1: Check if column exists and handle accordingly
DO $$
BEGIN
    -- If column doesn't exist, create it as array
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' 
        AND table_name = 'education' 
        AND column_name = 'department_scope'
    ) THEN
        ALTER TABLE education.education ADD COLUMN department_scope text[];
    ELSE
        -- Column exists, check if it's already array type
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'education' 
            AND table_name = 'education' 
            AND column_name = 'department_scope'
            AND data_type = 'ARRAY'
        ) THEN
            -- Already array type, do nothing
            RAISE NOTICE 'department_scope is already array type';
        ELSE
            -- Convert text to array
            -- Step 1: Add temporary column
            ALTER TABLE education.education ADD COLUMN department_scope_temp text[];
            
            -- Step 2: Convert existing JSON string data to array
            -- JSON format: ["인사팀", "개발팀"] -> array format: {인사팀,개발팀}
            UPDATE education.education
            SET department_scope_temp = 
              CASE 
                WHEN department_scope IS NULL OR department_scope = '' THEN NULL
                WHEN department_scope = 'null' THEN NULL
                ELSE 
                  -- Parse JSON array and convert to PostgreSQL array
                  ARRAY(
                    SELECT jsonb_array_elements_text(department_scope::jsonb)
                  )
              END
            WHERE department_scope IS NOT NULL;
            
            -- Step 3: Drop old column
            ALTER TABLE education.education DROP COLUMN department_scope;
            
            -- Step 4: Rename temp column to original name
            ALTER TABLE education.education RENAME COLUMN department_scope_temp TO department_scope;
        END IF;
    END IF;
END $$;

-- Add comment
COMMENT ON COLUMN education.education.department_scope IS '수강 가능한 부서 목록(배열)';

