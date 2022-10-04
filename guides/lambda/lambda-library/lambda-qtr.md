---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Data & Time functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## QTR

### About

Shows quarterly information from an array of dates.

#### Inputs:
  - s: function uses by default s=4 (April). This varies by country, but the most common s's are 4,7,10.
  - a: dates array
  - '[qt]': quarter type argument, text, could be one of these values:
     - "q": calendar quarter
     - "qy": calendar quarter and year, (year will be listed first for sorting versatility) ; format ex.: 2022 Q3
     - "fq": fiscal quarter : format ex.: FQ4
     - "fy": fiscal year ; format ex.: FY 2023 (represents fiscal year 2022-2023)
     - "fqy": fiscal quarter and year, (year will be listed first for sorting versatility) ; format ex.: FY 2022 Q2

#### More Info:
  - if qt is omitted, function will calculate "qy", if qt<> above values function returns #NA() error

### Code

{% capture code %}
QTR = LAMBDA(a, [qt],
    LET(
        s, 4,
        m, MONTH(a),
        y, YEAR(a),
        q, "Q" & MONTH(m & 0),
        qy, y & " " & q,
        fq, "Q" & MONTH(MOD(m - s, 12) + 1 & 0),
        fy, "FY " & y + (m >= s),
        fqy, fy & " " & fq,
        SWITCH(qt, 0, qy, "q", q, "qy", qy, "fq", "F" & fq, "fy", fy, "fqy", fqy)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}