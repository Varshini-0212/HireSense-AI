from __future__ import annotations

from typing import Dict, Iterable, List, Optional, Set, Tuple
import re

from sklearn.feature_extraction.text import ENGLISH_STOP_WORDS, TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

from .nlp.parser import extract_education, extract_experience_years, extract_skills
from .nlp.skills import ALIAS_TO_CANONICAL


def _normalize_skill(value: str) -> Optional[str]:
    key = re.sub(r"\s+", " ", value.strip().lower())
    return ALIAS_TO_CANONICAL.get(key, key or None)


def _canonical(values: Iterable[str]) -> Set[str]:
    return {skill for value in values if (skill := _normalize_skill(str(value)))}


def _lemma(token: str) -> str:
    """Normalize common English inflections without a downloaded language corpus."""
    if len(token) > 5 and token.endswith("ies"):
        return token[:-3] + "y"
    if len(token) > 5 and token.endswith("ing"):
        root = token[:-3]
        if len(root) > 2 and root[-1] == root[-2]:
            root = root[:-1]
        elif root.endswith(("us", "at", "iz", "ag", "ir")):
            root += "e"
        return root
    if len(token) > 4 and token.endswith("ed"):
        root = token[:-2]
        if root.endswith(("at", "iz", "ag", "ir")):
            root += "e"
        return root
    if len(token) > 3 and token.endswith("s") and not token.endswith(("ss", "us", "is")):
        return token[:-1]
    return token


def _preprocess(text: str) -> str:
    tokens = [token for token in re.findall(r"[a-z0-9]+", text.lower()) if token not in ENGLISH_STOP_WORDS]
    return " ".join(_lemma(token) for token in tokens)


def semantic_score(candidate: str, job: str) -> float:
    if not candidate.strip() or not job.strip():
        return 0.0
    vectorizer = TfidfVectorizer(preprocessor=_preprocess, lowercase=False,
                                 ngram_range=(1, 2), sublinear_tf=True, max_features=12_000)
    try:
        matrix = vectorizer.fit_transform([candidate, job])
    except ValueError:
        return 0.0
    return float(cosine_similarity(matrix[0:1], matrix[1:2])[0, 0])


def experience_score(candidate_years: Optional[float], required_years: Optional[float]) -> float:
    if required_years is None or required_years <= 0:
        return 1.0
    if candidate_years is None:
        return 0.0
    return min(1.0, max(0.0, candidate_years / required_years))


def education_score(candidate: Optional[str], required: Optional[str], candidate_text: str, job_text: str) -> float:
    candidate_value = (candidate or " ".join(extract_education(candidate_text))).lower()
    required_value = (required or " ".join(extract_education(job_text))).lower()
    if not required_value:
        return 1.0
    if not candidate_value:
        return 0.0
    required_tokens = set(re.findall(r"[a-z0-9]+", required_value))
    candidate_tokens = set(re.findall(r"[a-z0-9]+", candidate_value))
    if required_tokens.issubset(candidate_tokens) or required_value in candidate_value:
        return 1.0
    if required_tokens & candidate_tokens:
        return 0.5
    return 0.0


def match_candidate(*, resume_text: str, job_text: str, resume_skills: List[str], required_skills: List[str], preferred_skills: List[str], candidate_experience_years: Optional[float], required_experience_years: Optional[float], candidate_education: Optional[str], required_education: Optional[str], weights: Dict[str, float]) -> Dict[str, object]:
    candidate_found = _canonical(resume_skills) | _canonical(extract_skills(resume_text))
    required_found = _canonical(required_skills) or _canonical(extract_skills(job_text))
    preferred_found = _canonical(preferred_skills)
    target_found = required_found | preferred_found
    matched = sorted(candidate_found & target_found)
    missing = sorted(required_found - candidate_found)
    skill_base = len(candidate_found & required_found) / len(required_found) if required_found else 1.0
    # Preferred skills improve the skill component modestly without overriding required skills.
    if preferred_found:
        skill_component = min(1.0, 0.8 * skill_base + 0.2 * (len(candidate_found & preferred_found) / len(preferred_found)))
    else:
        skill_component = skill_base
    candidate_years = candidate_experience_years if candidate_experience_years is not None else extract_experience_years(resume_text)
    required_years = required_experience_years if required_experience_years is not None else extract_experience_years(job_text)
    sections = {
        "semantic": round(semantic_score(resume_text, job_text) * 100, 2),
        "skills": round(skill_component * 100, 2),
        "experience": round(experience_score(candidate_years, required_years) * 100, 2),
        "education": round(education_score(candidate_education, required_education, resume_text, job_text) * 100, 2),
    }
    total = sum(weights.values()) or 1.0
    normalized = {key: value / total for key, value in weights.items()}
    overall = round(sum(sections[key] * normalized.get(key, 0) for key in sections), 2)
    feedback = []
    if sections["semantic"] < 50: feedback.append("Resume language has limited overlap with the job description.")
    if missing: feedback.append(f"Required skills missing: {', '.join(missing)}.")
    if sections["experience"] < 100: feedback.append("Experience evidence is below the stated requirement or was not supplied.")
    if sections["education"] < 100: feedback.append("Education evidence does not fully match the stated requirement.")
    if not feedback: feedback.append("Resume demonstrates strong alignment across the evaluated sections.")
    tips = []
    if missing: tips.append("Add truthful project or work examples demonstrating the missing required skills.")
    if sections["semantic"] < 70: tips.append("Use job-relevant terminology in the summary and achievement bullets where accurate.")
    if sections["experience"] < 100: tips.append("Quantify relevant experience and clarify duration for each role.")
    if sections["education"] < 100: tips.append("State the degree, field, and institution explicitly if relevant.")
    if not tips: tips.append("Keep achievements quantified and tailor the most relevant evidence to the role.")
    return {"overall_score": overall, "component_scores": sections, "matched_skills": matched, "missing_skills": missing, "section_feedback": feedback, "improvement_tips": tips}
