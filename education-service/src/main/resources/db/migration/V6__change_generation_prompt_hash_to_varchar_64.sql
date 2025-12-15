-- Align column type with JPA expectation (varchar(64))
ALTER TABLE education.education_script
  ALTER COLUMN generation_prompt_hash TYPE varchar(64);

