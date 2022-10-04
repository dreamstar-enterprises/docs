---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## AFILTER

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/afilter.1164693/){:target="_blank"}

### About

Filters array by rows, text, non blanks, or numbers, left aligned.

#### Inputs:

   - ar - required. Array to be filtered
   - k - required. Filter argument -1 text, 0 non blanks, 1 numbers
   - nf - optional. String message if not found

### Code

{% capture code %}
AFILTER = LAMBDA(ar, k, nf,
    LET(
        xk, OR(k = {-1, 0, 1}),
        r, ROWS(ar),
        c, COLUMNS(ar),
        sr, SEQUENCE(r),
        s, SEQUENCE(r * c),
        q, QUOTIENT(s - 1, c) + 1,
        m, MOD(s - 1, c) + 1,
        a, INDEX(IF(ar = "", "", ar), q, m),
        x, a <> "",
        f, SWITCH(
            k,
            -1,
            x * ISTEXT(a),
            0,
            --x,
            1,
            x * ISNUMBER(a)
        ),
        na, FILTER(a, f),
        nq, FILTER(q, f),
        fq, FREQUENCY(nq, sr),
        p, INDEX(fq, sr),
        nc, MAX(p),
        nsa, IF(p >= SEQUENCE(, nc), SEQUENCE(r, nc)),
        nsr, SMALL(nsa, SEQUENCE(SUM(p))),
        rs, IFNA(XLOOKUP(nsa, nsr, na), ""),
        IF(
            xk,
            IFERROR(rs, IF(nf = "", "", nf)),
            "check var -1 (only txt), 0 (no blnks), 1 (only nr.)"
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}