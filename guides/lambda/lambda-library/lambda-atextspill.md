---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array 'By Element' functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ATEXTSPILL

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/atextsplit.1172428/page-4#posts){:target="_blank"}

### About

Applies a a helper lambda function by row, on a 1-D column vector.

That is, expands a column vector into rows (depending on the function passed into it).

#### Inputs:

   - cl - column vector
   - fn - lambda helper function argument: e.g. LAMBDA(x,TEXTSPLIT(x,...)), or TEXTBEFORE(x,...
   - [nf] - not found argument: if omitted => empty string ""

### Code

{% capture code %}
ATEXTSPILL = LAMBDA(cl, fn, [nf],
    LET(
        e, IF(ISOMITTED(nf), "", nf),
        r, REDUCE(
            0,
            SEQUENCE(ROWS(cl)),
            LAMBDA(v, i, VSTACK(v, IFERROR(fn(INDEX(cl, i, 1)), e)))
        ),
        t, IFNA(DROP(r, 1), ""),
        a, IF(AND(t = ""), e, t),
        IFERROR(--a, a)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}