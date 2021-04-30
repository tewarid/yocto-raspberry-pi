function Math(meta)
    --print(meta.mathtype.." "..meta.text)
    -- force InlineMath
    meta.mathtype = "InlineMath"
    return meta
end
