import os

for subdir, dirs, files in os.walk('.'):
    for file in files:
        print(os.path.join(subdir, file))
        filepath = subdir + os.sep + file

        if filepath.endswith(".mid"):
            print (filepath)
            outfile = filepath[:-3] + "notelist"
            cmd = "~/Desktop/mftext \"" + filepath + "\" > \"" + outfile + "\""
            print(cmd)
            os.system(cmd)
