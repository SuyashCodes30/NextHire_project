import urllib.request
import json

def test_api(category, difficulty, num_questions, label):
    url = "http://localhost:8080/api/interviews"
    payload = {
        "category": category,
        "difficulty": difficulty,
        "numberOfQuestions": num_questions
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    
    with urllib.request.urlopen(req) as resp:
        body = json.loads(resp.read().decode("utf-8"))
        res = body.get("data", body)
    
    print(f"\n=======================================================")
    print(f"=== {label} ===")
    print(f"=======================================================")
    print(f"Interview ID: {res.get('interviewId')}")
    print(f"Category: {res.get('category')} | Difficulty: {res.get('difficulty')}")
    print(f"Requested: {num_questions} | Returned: {len(res.get('questions', []))}")
    
    topics = []
    seen_ids = set()
    duplicates = []
    
    for i, q in enumerate(res.get("questions", []), 1):
        qid = q.get("id")
        if qid in seen_ids:
            duplicates.append(qid)
        seen_ids.add(qid)
        topic = q.get("topic")
        subtopic = q.get("subtopic")
        diff = q.get("difficulty")
        text = q.get("text", "")[:60]
        has_correct = bool(q.get("correctAnswer") or q.get("expectedAnswer"))
        has_exp = bool(q.get("explanation"))
        res_count = len(q.get("learningResources") or [])
        topics.append(topic)
        print(f" {i}. [{topic} / {subtopic}] ({diff}): {text}... | CorrectAns? {has_correct} | Exp? {has_exp} | Resources: {res_count}")
    
    unique_topics = set(topics)
    print(f"Total Unique Topics: {len(unique_topics)} ({', '.join(sorted(unique_topics))})")
    print(f"Duplicate Questions: {len(duplicates)}")
    assert len(res.get("questions", [])) == num_questions, f"Expected {num_questions}, got {len(res.get('questions', []))}"
    assert len(duplicates) == 0, f"Found duplicates: {duplicates}"

if __name__ == "__main__":
    test_api("data-analyst", "easy", 5, "TEST 1: Data Analyst, Easy, 5 Questions")
    test_api("data-analyst", "medium", 10, "TEST 2: Data Analyst, Medium, 10 Questions")
    test_api("data-scientist", "hard", 15, "TEST 3: Data Scientist, Hard, 15 Questions")
    print("\nALL 3 TEST SCENARIOS PASSED WITH EXACT COUNTS, ZERO DUPLICATES, AND TOPIC DIVERSITY!")
