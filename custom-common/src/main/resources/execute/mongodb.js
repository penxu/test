db.email_settings.insert([{
    "type": "operator",
    "operator": [
        {
            "label": "发件人",
            "type": "text",
            "value": "from_recipient:false"
            "operator": [
                {
                    "label": "包含",
                    "type": "CONTAIN",
                    "input": "text"
                },
                {
                    "label": "不包含",
                    "type": "NCONTAIN",
                    "input": "text"
                }
            ]
        },
        {
            "label": "收件人",
            "type": "text",
            "value": "to_recipients:false"
            "operator": [
                {
                    "label": "包含",
                    "type": "CONTAIN",
                    "input": "text"
                },
                {
                    "label": "不包含",
                    "type": "NCONTAIN",
                    "input": "text"
                }
            ]
        },
        {
            "label": "主题",
            "type": "text",
            "value": "subject:false"
            "operator": [
                {
                    "label": "包含",
                    "type": "CONTAIN",
                    "input": "TEXT"
                },
                {
                    "label": "不包含",
                    "type": "NCONTAIN",
                    "input": "TEXT"
                }
            ]
        },
        {
            "label": "邮件大小",
            "type": "number",
            "value": "mail_size:false"
            "operator": [
                {
                    "label": "大于",
                    "type": "GREATER",
                    "input": "NUMBER"
                },
                {
                    "label": "小于",
                    "type": "LESS",
                    "input": "NUMBER"
                },
                {
                    "label": "等于",
                    "type": "EQUALS",
                    "input": "NUMBER"
                },
                {
                    "label": "大于等于",
                    "type": "GREATERE",
                    "input": "NUMBER"
                },
                {
                    "label": "小于等于",
                    "type": "LESSE",
                    "input": "NUMBER"
                }
            ]
        }
    ]
}]);
db.setting.insert([
{
    "type" : "operator",
    "operator" : [ 
        {
            "type" : [ 
                "text", 
                "textarea", 
                "phone", 
                "email", 
                "url", 
                "identifier", 
                "location", 
                "reference", 
                "area"
            ],
            "operator" : [ 
                {
                    "label" : "包含",
                    "type" : "CONTAIN",
                    "input" : "TEXT"
                }, 
                {
                    "label" : "不包含",
                    "type" : "NCONTAIN",
                    "input" : "TEXT"
                }, 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "TEXT"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "TEXT"
                }, 
                {
                    "label" : "为空",
                    "type" : "ISNULL"
                }, 
                {
                    "label" : "不为空",
                    "type" : "ISNOTNULL"
                }, 
                {
                    "label" : "起始字符",
                    "type" : "PREFIX",
                    "input" : "TEXT"
                }
            ]
        }, 
        {
            "type" : [ 
                "picklist", 
                "multi"
            ],
            "valueField" : "value",
            "operator" : [ 
                {
                    "label" : "包含",
                    "type" : "CONTAIN",
                    "input" : "SELECT"
                }, 
                {
                    "label" : "不包含",
                    "type" : "NCONTAIN",
                    "input" : "SELECT"
                }, 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "SELECT"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "SELECT"
                }, 
                {
                    "label" : "为空",
                    "type" : "ISNULL"
                }, 
                {
                    "label" : "不为空",
                    "type" : "ISNOTNULL"
                }
            ]
        }, 
        {
            "type" : [ 
                "number", 
                "formula"
            ],
            "operator" : [ 
                {
                    "label" : "大于",
                    "type" : "GREATER",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "小于",
                    "type" : "LESS",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "大于等于",
                    "type" : "GREATERE",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "小于等于",
                    "type" : "LESSE",
                    "input" : "NUMBER"
                }, 
                {
                    "label" : "为空",
                    "type" : "ISNULL"
                }, 
                {
                    "label" : "不为空",
                    "type" : "ISNOTNULL"
                }
            ]
        }, 
        {
            "type" : [ 
                "datetime"
            ],
            "operator" : [ 
                {
                    "label" : "位于",
                    "type" : "BETWEEN",
                    "input" : "DATE"
                }, 
                {
                    "label" : "以前",
                    "type" : "BEFORE",
                    "input" : "DATE"
                }, 
                {
                    "label" : "以后",
                    "type" : "AFTER",
                    "input" : "DATE"
                }, 
                {
                    "label" : "今日",
                    "type" : "TODAY"
                }, 
                {
                    "label" : "本周",
                    "type" : "WEEK"
                }, 
                {
                    "label" : "本月",
                    "type" : "MONTH"
                }, 
                {
                    "label" : "本季度",
                    "type" : "QUARTER"
                }, 
                {
                    "label" : "本年",
                    "type" : "YEAR"
                }, 
                {
                    "label" : "为空",
                    "type" : "ISNULL"
                }, 
                {
                    "label" : "不为空",
                    "type" : "ISNOTNULL"
                }
            ]
        }, 
        {
            "type" : [ 
                "personnel"
            ],
            "valueField" : "id",
            "operator" : [ 
                {
                    "label" : "包含",
                    "type" : "CONTAIN",
                    "input" : "ERSONNEL"
                }, 
                {
                    "label" : "不包含",
                    "type" : "NCONTAIN",
                    "input" : "ERSONNEL"
                }, 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "ERSONNEL"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "ERSONNEL"
                }, 
                {
                    "label" : "为空",
                    "type" : "ISNULL"
                }, 
                {
                    "label" : "不为空",
                    "type" : "ISNOTNULL"
                }
            ]
        }, 
        {
            "type" : [ 
                "role"
            ],
            "operator" : [ 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "ROLE"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "ROLE"
                }
            ]
        }, 
        {
            "type" : [ 
                "department"
            ],
            "operator" : [ 
                {
                    "label" : "等于",
                    "type" : "EQUALS",
                    "input" : "DEPARTMENT"
                }, 
                {
                    "label" : "不等于",
                    "type" : "NEQUALS",
                    "input" : "DEPARTMENT"
                }
            ]
        }
    ]
}]);
