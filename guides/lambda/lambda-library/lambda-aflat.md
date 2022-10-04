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

## AFLAT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aflat.1175181/){:target="_blank"}

### About

Flattens any array and filters out blanks, null strings, errors.

#### Inputs:

  - a - the array
  - ib - "include blanks"

#### More Info:

  - ib = 0, or omitted, "include blanks?",, excludes blanks/null strings and errors.
  - ib = 1 includes null strings/blanks, errors are replaced by null strings.
  - ib <> {0,1}, returns "check arg." 

*NOTE:* this is slightly better than the new native TOCOL function as it can properly exclude null strings, e.g. "", and blanks are not returned as 0s


### Code

{% capture code %}
AFLAT = LAMBDA(a, [ib],
    IF(
        AND(ib <> {0, 1}),
        "check arguments",
        LET(
            r, ROWS(a),
            c, COLUMNS(a),
            s, SEQUENCE(r * c),
            q, QUOTIENT(s - 1, c) + 1,
            m, MOD(s - 1, c) + 1,
            x, INDEX(IFERROR(IF(a = "", "", a), ""), q, m),
            FILTER(x, IF(ib, TRUE, x <> ""))
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}