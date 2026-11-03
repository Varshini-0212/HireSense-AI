from __future__ import annotations

import re
from collections import Counter
from typing import Dict, List

from .skills import ALIAS_TO_CANONICAL

SECTION_HEADERS = {
    "summary": r"summary|profile|objective|about me",
    "experience": r"experience|employment|work history|professional history",
    "education": r"education|academic background|qualifications",
    "skills": r"skills|technical skills|core competencies|competencies",
    "certifications": r"certifications?|licenses?",
    "projects": r"projects?|portfolio",
}
EDUCATION_TERMS = ("phd", "doctorate", "master", "msc", "mba", "bachelor", "bsc", "bs ", "ba ", "associate", "diploma", "b.tech", "m.tech", "jd", "md", "llb", "llm")
STOPWORDS = set("a an and are as at be by for from has have in is it of on or our that the their this to with you your we will using use used into over under".split())


def normalize_text(text: str) -> str:
    return re.sub(r"\s+", " ", text.lower()).strip()


def extract_skills(text: str) -> List[str]:
    normalized = normalize_text(text)
    found = set()
    for alias, canonical in sorted(ALIAS_TO_CANONICAL.items(), key=lambda item: len(item[0]), reverse=True):
        pattern = r"(?<![a-z0-9+#.])" + re.escape(alias) + r"(?![a-z0-9+#.])"
        if re.search(pattern, normalized):
            found.add(canonical)
    return sorted(found)


def extract_experience_years(text: str):
    candidates = [float(m.group(1)) for m in re.finditer(r"(?<!\d)(\d+(?:\.\d+)?)\s*\+?\s*(?:years?|yrs?)(?![a-z])", normalize_text(text))]
    return max(candidates) if candidates else None


def extract_education(text: str) -> List[str]:
    normalized = normalize_text(text)
    result = []
    for term in EDUCATION_TERMS:
        if re.search(r"(?<![a-z])" + re.escape(term.strip()) + r"(?![a-z])", normalized):
            result.append(term.strip())
    return sorted(set(result))


def extract_sections(text: str) -> Dict[str, str]:
    lines = [line.strip() for line in text.splitlines()]
    sections: Dict[str, List[str]] = {key: [] for key in SECTION_HEADERS}
    current = "summary"
    for line in lines:
        if not line:
            continue
        heading = re.sub(r"[:\-]+$", "", line.lower()).strip()
        matched = next((name for name, pattern in SECTION_HEADERS.items() if re.fullmatch(pattern, heading)), None)
        if matched:
            current = matched
        else:
            sections[current].append(line)
    return {key: "\n".join(value).strip() for key, value in sections.items() if value}


def extract_list_section(text: str, section: str) -> List[str]:
    content = extract_sections(text).get(section, "")
    return [re.sub(r"^[\-•*\d.) ]+", "", line).strip() for line in content.splitlines() if line.strip()]


def extract_contact(text: str):
    email = next(iter(re.findall(r"[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}", text, flags=re.I)), None)
    phone = next(iter(re.findall(r"(?:\+?\d[\d() .-]{7,}\d)", text)), None)
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    name = None
    if lines and not re.search(r"@|resume|curriculum|cv|\d", lines[0], re.I) and len(lines[0].split()) <= 5:
        name = lines[0]
    return name, email, phone


def parse_text(text: str) -> Dict[str, object]:
    name, email, phone = extract_contact(text)
    return {
        "extracted_name": name,
        "email": email,
        "phone": phone,
        "skills": extract_skills(text),
        "education": extract_education(text),
        "certifications": extract_list_section(text, "certifications"),
        "projects": extract_list_section(text, "projects"),
        "years_experience": extract_experience_years(text),
    }
