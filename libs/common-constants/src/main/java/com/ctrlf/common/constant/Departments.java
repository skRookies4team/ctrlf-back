package com.ctrlf.common.constant;

import java.util.List;

/**
 * 공통 부서 상수.
 * 화면 라벨/필터 등에 재사용합니다.
 */
public final class Departments {
    private Departments() {}

    public static final String ALL = "전체 부서";
    public static final String GENERAL_AFFAIRS = "총무팀";
    public static final String PLANNING = "기획팀";
    public static final String MARKETING = "마케팅팀";
    public static final String HR = "인사팀";
    public static final String FINANCE = "재무팀";
    public static final String ENGINEERING = "개발팀";
    public static final String SALES = "영업팀";
    public static final String LEGAL = "법무팀";

    /**
     * UI 드롭다운 등에서 사용할 기본 표시 순서.
     */
    public static final List<String> DEFAULT_ORDER = List.of(
        ALL,
        GENERAL_AFFAIRS,
        PLANNING,
        MARKETING,
        HR,
        FINANCE,
        ENGINEERING,
        SALES,
        LEGAL
    );
}

