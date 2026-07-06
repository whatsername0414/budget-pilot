# Repair v1

Your previous response failed to parse as valid JSON matching the required
schema. Fix it and respond with **corrected JSON only** — no prose, no
markdown code fences, no explanation before or after.

## Required schema

```json
{
  "receipt_type": "PAPER" | "GCASH" | "MAYA",
  "merchant": { "value": string, "confidence": "HIGH" | "MEDIUM" | "LOW" },
  "date": { "value": "YYYY-MM-DD", "confidence": "HIGH" | "MEDIUM" | "LOW" },
  "line_items": {
    "value": [ { "description": string, "amount": number } ],
    "confidence": "HIGH" | "MEDIUM" | "LOW"
  },
  "total": { "value": number, "confidence": "HIGH" | "MEDIUM" | "LOW" },
  "suggested_category": { "value": string | null, "confidence": "HIGH" | "MEDIUM" | "LOW" }
}
```

Common mistakes to check for and fix:
- Trailing commas, unescaped quotes, or missing braces/brackets.
- Markdown code fences (` ```json ... ``` `) wrapping the JSON — remove them.
- Extra prose before or after the JSON object — remove it.
- Amounts written as strings or with a currency symbol/thousands separator
  (e.g. `"₱1,845.32"`) — convert to a plain number (`1845.32`).
- A missing required key — infer the most reasonable value from the rest of
  the malformed output, or use `null` only where the schema allows it.

## Malformed output

{{malformed_output}}
